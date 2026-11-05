package com.roommate;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

final class AddPreferencesHandler {

    private final SessionStore sessions;

    AddPreferencesHandler(SessionStore sessions) {
        this.sessions = sessions;
    }

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        // Get the session cookie and find out who is logged in
        String sessionId = ApiHandler.readCookie(exchange, "sessionId");
        String email = sessions.getEmail(sessionId);

        if (email == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        JsonObject body = ApiHandler.readBody(exchange);
        
        // 4. TODO: Save to PreferencesDatabase using the 'email'
        
        api.sendJson(exchange, 501, api.error("handleAddPreferences received:\n" + body));
    }


}
