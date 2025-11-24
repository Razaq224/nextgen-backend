package com.nextgenhealthcare.websocket;

import org.springframework.web.socket.WebSocketSession;

/**
 * Represents a participant in a video call room.
 */
public class Participant {
    private final WebSocketSession session;
    private final String userId;
    private final String role;
    private final String displayName;

    public Participant(WebSocketSession session, String userId, String role, String displayName) {
        this.session = session;
        this.userId = userId;
        this.role = role;
        this.displayName = displayName;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getDisplayName() {
        return displayName;
    }
}

