package com.roommate;

import com.sun.net.httpserver.Headers;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

final class SessionHandler {

    private final SessionStore sessions;

    SessionHandler(SessionStore sessions) {
        this.sessions = sessions;
    }

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use GET for this endpoint"));
            return;
        }

        String sessionId = readCookie(exchange, "sessionId");
        String email = sessions.getEmail(sessionId);
        if (email == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("ok", true);
        payload.addProperty("email", email);
        api.sendJson(exchange, 200, payload);
    }

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
