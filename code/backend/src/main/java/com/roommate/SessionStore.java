package com.roommate;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * SessionStore
 */
public class SessionStore {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    /**
     * Open a new user session via e-mail address.
     * @param email User's e-mail address.
     * @return The session ID String.
     */
    public String createSession(String email) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, email);
        return sessionId;
    }

    /**
     * Get the e-mail address from a session ID.
     * @param sessionId Session ID String.
     * @return The full user e-mail address.
     */
    public String getEmail(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        return sessions.get(sessionId);
    }

    /**
     * Expire a session by its ID.
     * @param sessionId The session ID to close.
     */
    public void removeSession(String sessionId) {
        if (sessionId != null) {
            sessions.remove(sessionId);
        }
    }
}
