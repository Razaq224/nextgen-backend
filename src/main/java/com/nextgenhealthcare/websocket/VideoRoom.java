package com.nextgenhealthcare.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a video call room that can hold up to 2 participants.
 */
public class VideoRoom {
    private final String roomId;
    private final Map<String, Participant> participants = new ConcurrentHashMap<>();
    private static final int MAX_PARTICIPANTS = 2;

    public VideoRoom(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomId() {
        return roomId;
    }

    public Map<String, Participant> getParticipants() {
        return participants;
    }

    public boolean isFull() {
        return participants.size() >= MAX_PARTICIPANTS;
    }

    public boolean isEmpty() {
        return participants.isEmpty();
    }

    public int getParticipantCount() {
        return participants.size();
    }

    public Participant addParticipant(String sessionId, Participant participant) {
        if (isFull()) {
            return null;
        }
        return participants.put(sessionId, participant);
    }

    public Participant removeParticipant(String sessionId) {
        return participants.remove(sessionId);
    }

    public Participant getOtherParticipant(String excludeSessionId) {
        return participants.values().stream()
                .filter(p -> !p.getSession().getId().equals(excludeSessionId))
                .findFirst()
                .orElse(null);
    }
}

