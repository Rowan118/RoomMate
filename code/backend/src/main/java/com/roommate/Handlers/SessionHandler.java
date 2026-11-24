package com.roommate.Handlers;

import com.google.gson.JsonObject;
import com.roommate.Services.SessionStore;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * SessionHandler
 */
final class SessionHandler implements ApiEndpoint {

    private final SessionStore sessions;

    /**
     * Create a new SessionHandler instance.
     * @param sessions A SessionStore instance.
     */
    SessionHandler(SessionStore sessions) {
        this.sessions = sessions;
    }

    /**
     * Handle API requests for sessions.
     * @param api The API request.
     * @param exchange The http exchange request.
     * @throws Exception May fail if there's an unexpected network error.
     */
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        String sessionId = ApiRouter.readCookie(exchange, "sessionId");
        String email = sessions.getEmail(sessionId);
        if (email == null) {
            ApiRouter.sendJson(exchange, 401, ApiRouter.error("Not logged in"));
            return;
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("ok", true);
        payload.addProperty("email", email);
        ApiRouter.sendJson(exchange, 200, payload);
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/api/session";
    }

}
