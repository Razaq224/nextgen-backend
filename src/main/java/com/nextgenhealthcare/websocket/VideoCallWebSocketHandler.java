package com.nextgenhealthcare.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for WebRTC video call signaling.
 * Supports one-to-one video calls with room-based connection management.
 */
@Component
public class VideoCallWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(VideoCallWebSocketHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, VideoRoom> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connection established: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            String type = payload.path("type").asText();

            log.debug("Received message type '{}' from session {}", type, session.getId());

            switch (type) {
                case "join" -> handleJoin(session, payload);
                case "offer" -> handleOffer(session, payload);
                case "answer" -> handleAnswer(session, payload);
                case "ice-candidate" -> handleIceCandidate(session, payload);
                case "leave" -> handleLeave(session);
                case "ping" -> sendMessage(session, buildMessage("pong"));
                default -> log.warn("Unsupported message type '{}' from session {}", type, session.getId());
            }
        } catch (Exception e) {
            log.error("Error processing message from session {}", session.getId(), e);
            sendError(session, "PROCESSING_ERROR", "Failed to process message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket connection closed: {} ({})", session.getId(), status);
        removeFromRoom(session);
    }

    /**
     * Handles a participant joining a room.
     * Enforces 2-participant limit per room.
     */
    private void handleJoin(WebSocketSession session, JsonNode payload) throws IOException {
        String roomId = payload.path("roomId").asText(null);
        String userId = payload.path("userId").asText(null);
        String role = payload.path("role").asText("GUEST");
        String displayName = payload.path("displayName").asText("Unknown");

        if (roomId == null || roomId.isBlank() || userId == null || userId.isBlank()) {
            log.warn("Invalid join payload from session {}: missing roomId or userId", session.getId());
            sendError(session, "INVALID_JOIN", "roomId and userId are required");
            return;
        }

        VideoRoom room = rooms.computeIfAbsent(roomId, VideoRoom::new);

        // Check if room is full
        if (room.isFull() && !room.getParticipants().containsKey(session.getId())) {
            log.warn("Room {} is full. Cannot join session {}", roomId, session.getId());
            sendError(session, "ROOM_FULL", "Room is full (maximum 2 participants)");
            return;
        }

        // Store session attributes
        session.getAttributes().put("roomId", roomId);
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("role", role);
        session.getAttributes().put("displayName", displayName);

        Participant participant = new Participant(session, userId, role, displayName);
        room.addParticipant(session.getId(), participant);

        log.info("User {} ({}) joined room {}. Room now has {} participant(s)", 
                userId, displayName, roomId, room.getParticipantCount());

        // Send join confirmation
        ObjectNode joinedMessage = buildMessage("joined");
        joinedMessage.put("roomId", roomId);
        joinedMessage.put("userId", userId);
        sendMessage(session, joinedMessage);

        // Notify other participant if exists
        Participant otherParticipant = room.getOtherParticipant(session.getId());
        if (otherParticipant != null) {
            ObjectNode participantJoined = buildMessage("participant-joined");
            participantJoined.put("userId", userId);
            participantJoined.put("role", role);
            participantJoined.put("displayName", displayName);
            sendMessage(otherParticipant.getSession(), participantJoined);

            // Also notify the new participant about the existing one
            ObjectNode existingParticipant = buildMessage("participant-joined");
            existingParticipant.put("userId", otherParticipant.getUserId());
            existingParticipant.put("role", otherParticipant.getRole());
            existingParticipant.put("displayName", otherParticipant.getDisplayName());
            sendMessage(session, existingParticipant);
        }
    }

    /**
     * Handles WebRTC offer SDP.
     * Broadcasts ONLY to the other participant in the room.
     */
    private void handleOffer(WebSocketSession session, JsonNode payload) {
        VideoRoom room = getRoomFor(session);
        if (room == null) {
            sendError(session, "ROOM_NOT_FOUND", "Join a room before sending offer");
            return;
        }

        JsonNode sdp = payload.get("sdp");
        if (sdp == null) {
            sendError(session, "INVALID_OFFER", "Missing SDP in offer");
            return;
        }

        Participant otherParticipant = room.getOtherParticipant(session.getId());
        if (otherParticipant == null) {
            log.warn("No other participant in room {} to send offer to", room.getRoomId());
            return;
        }

        String userId = getSessionAttribute(session, "userId");
        ObjectNode offerMessage = buildMessage("offer");
        offerMessage.set("sdp", sdp);
        offerMessage.put("fromUserId", userId);

        log.debug("Forwarding offer from {} to other participant in room {}", userId, room.getRoomId());
        sendMessage(otherParticipant.getSession(), offerMessage);
    }

    /**
     * Handles WebRTC answer SDP.
     * Broadcasts ONLY to the other participant in the room.
     */
    private void handleAnswer(WebSocketSession session, JsonNode payload) {
        VideoRoom room = getRoomFor(session);
        if (room == null) {
            sendError(session, "ROOM_NOT_FOUND", "Join a room before sending answer");
            return;
        }

        JsonNode sdp = payload.get("sdp");
        if (sdp == null) {
            sendError(session, "INVALID_ANSWER", "Missing SDP in answer");
            return;
        }

        Participant otherParticipant = room.getOtherParticipant(session.getId());
        if (otherParticipant == null) {
            log.warn("No other participant in room {} to send answer to", room.getRoomId());
            return;
        }

        String userId = getSessionAttribute(session, "userId");
        ObjectNode answerMessage = buildMessage("answer");
        answerMessage.set("sdp", sdp);
        answerMessage.put("fromUserId", userId);

        log.debug("Forwarding answer from {} to other participant in room {}", userId, room.getRoomId());
        sendMessage(otherParticipant.getSession(), answerMessage);
    }

    /**
     * Handles ICE candidate exchange.
     * Broadcasts ONLY to the other participant in the room.
     */
    private void handleIceCandidate(WebSocketSession session, JsonNode payload) {
        VideoRoom room = getRoomFor(session);
        if (room == null) {
            sendError(session, "ROOM_NOT_FOUND", "Join a room before sending ICE candidate");
            return;
        }

        JsonNode candidate = payload.get("candidate");
        if (candidate == null) {
            sendError(session, "INVALID_ICE_CANDIDATE", "Missing candidate in ice-candidate");
            return;
        }

        Participant otherParticipant = room.getOtherParticipant(session.getId());
        if (otherParticipant == null) {
            // Log but don't error - ICE candidates can arrive before both participants join
            log.debug("No other participant in room {} to send ICE candidate to", room.getRoomId());
            return;
        }

        String userId = getSessionAttribute(session, "userId");
        ObjectNode candidateMessage = buildMessage("ice-candidate");
        candidateMessage.set("candidate", candidate);
        candidateMessage.put("fromUserId", userId);

        sendMessage(otherParticipant.getSession(), candidateMessage);
    }

    /**
     * Handles participant leaving the room.
     */
    private void handleLeave(WebSocketSession session) {
        log.info("Leave message received from session {}", session.getId());
        removeFromRoom(session);
        try {
            if (session.isOpen()) {
                session.close(CloseStatus.NORMAL);
            }
        } catch (IOException ex) {
            log.warn("Unable to close WebSocket session {}", session.getId(), ex);
        }
    }

    /**
     * Removes a participant from their room and cleans up if empty.
     */
    private void removeFromRoom(WebSocketSession session) {
        String roomId = getSessionAttribute(session, "roomId");
        if (roomId == null) {
            return;
        }

        VideoRoom room = rooms.get(roomId);
        if (room == null) {
            return;
        }

        Participant removed = room.removeParticipant(session.getId());
        if (removed != null) {
            String userId = removed.getUserId();
            log.info("User {} left room {}", userId, roomId);

            // Notify other participant
            Participant otherParticipant = room.getOtherParticipant(session.getId());
            if (otherParticipant != null) {
                ObjectNode participantLeft = buildMessage("participant-left");
                participantLeft.put("userId", userId);
                sendMessage(otherParticipant.getSession(), participantLeft);
            }
        }

        // Clean up empty room
        if (room.isEmpty()) {
            rooms.remove(roomId);
            log.debug("Removed empty room {}", roomId);
        }
    }

    /**
     * Sends a message to a WebSocket session.
     */
    private void sendMessage(WebSocketSession session, ObjectNode message) {
        if (!session.isOpen()) {
            log.debug("Cannot send message to closed session {}", session.getId());
            return;
        }
        try {
            session.sendMessage(new TextMessage(message.toString()));
        } catch (IOException ex) {
            log.warn("Failed to send WebSocket message to session {}", session.getId(), ex);
        }
    }

    /**
     * Sends an error message to a WebSocket session.
     */
    private void sendError(WebSocketSession session, String code, String detail) {
        ObjectNode error = buildMessage("error");
        error.put("code", code);
        error.put("message", detail);
        sendMessage(session, error);
        log.warn("Sent error to session {}: {} - {}", session.getId(), code, detail);
    }

    /**
     * Builds a base message object with the given type.
     */
    private ObjectNode buildMessage(String type) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("type", type);
        return node;
    }

    /**
     * Gets the room for a given session.
     */
    private VideoRoom getRoomFor(WebSocketSession session) {
        String roomId = getSessionAttribute(session, "roomId");
        return roomId != null ? rooms.get(roomId) : null;
    }

    /**
     * Gets a session attribute as a string.
     */
    private String getSessionAttribute(WebSocketSession session, String key) {
        Object value = session.getAttributes().get(key);
        return value != null ? value.toString() : null;
    }
}
