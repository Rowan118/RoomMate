package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

final class LoginHandler {

    private final UserDao users;
    private final SessionStore sessions;

    LoginHandler(UserDao users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        JsonObject body = readBody(exchange);
        String email = optString(body, "email");
        String password = optString(body, "password");

        if (email == null || password == null) {
            api.sendJson(exchange, 400, api.error("Email and password are required"));
            return;
        }

        if (!users.credentialsMatch(email, password)) {
            api.sendJson(exchange, 401, api.error("Invalid email or password"));
            return;
        }

        String sessionId = sessions.createSession(email);
        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=" + sessionId + "; Path=/; HttpOnly; SameSite=None; Secure"
        );

        api.sendJson(exchange, 200, api.success("Logged in"));
    }

    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    private String optString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
            ? obj.get(key).getAsString()
            : null;
    }
}
