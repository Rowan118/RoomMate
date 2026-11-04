package com.roommate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStore {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public String createSession(String email) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, email);
        return sessionId;
    }

    public String getEmail(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
}
