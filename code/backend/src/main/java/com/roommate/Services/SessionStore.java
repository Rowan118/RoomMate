package com.roommate.Services;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * SessionStore
 */
public class SessionStore {
    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private UserDao users;

    /**
     * Open a new user session via e-mail address.
     * @param email User's e-mail address.
     * @return The session ID String.
     */
    public String createSession(UserDao users, String email) {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, email);
        this.users = users;
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
     * Get the account type from a sesion ID.
     * @param sessionId Session ID String.
     * @return The account type as a String.
     */
    public String getAccountType(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        try {
            return this.users.getAccountTypeByEmail(getEmail(sessionId));
        }
        catch (SQLException e) {
            return null;
        }
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
