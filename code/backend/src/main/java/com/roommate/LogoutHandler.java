package com.roommate;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * LogoutHandler
 */
final class LogoutHandler {

    private final SessionStore sessions;

    /**
     * Create a LogoutHandler instance
     * @param sessions SessionStore of current sessions.
     */
    LogoutHandler(SessionStore sessions) {
        this.sessions = sessions;
    }

    /**
     * Handle API requests regarding logging out.
     * @param api APIHandler instance
     * @param exchange The http request
     * @throws Exception May throw an exception if an unexpected network error occurs.
     */
    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        String sessionId = readCookie(exchange, "sessionId");
        sessions.removeSession(sessionId);

        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"
        );

        api.sendJson(exchange, 200, api.success("Logged out"));
    }

    /**
     * Get a cookie from an http request and a key.
     * @param exchange Http request.
     * @param key Key
     * @return Cookie String
     */
    private String readCookie(HttpExchange exchange, String key) {
        Headers headers = exchange.getRequestHeaders();
        String cookieHeader = headers.getFirst("Cookie");
        if (cookieHeader == null) {
            return null;
        }

        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(key + "=")) {
                return trimmed.substring((key + "=").length());
            }
        }
        return null;
    }
}
