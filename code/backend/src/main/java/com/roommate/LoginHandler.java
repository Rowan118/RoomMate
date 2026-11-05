package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * LoginHandler
 */
final class LoginHandler {

    private final UserDao users;
    private final SessionStore sessions;

    /**
     * Create a new LoginHandler instance.
     * @param users The UserDao of users
     * @param sessions A SessionStore of sessions
     */
    LoginHandler(UserDao users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    /**
     * Handle login API requests.
     * @param api The APIHandler instance
     * @param exchange The http request
     * @throws Exception May throw an exception if an unexpected network error occurs.
     */
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

        sessions.removeSession(readCookie(exchange, "sessionId"));
        String sessionId = sessions.createSession(email);
        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax"
        );

        api.sendJson(exchange, 200, api.success("Logged in"));
    }

    /**
     * Read http body
     * @param exchange Http request object
     * @return Returns a JsonObject that neatly contains the http request's data.
     * @throws IOException May throw an IOException if bytes fail to be read.
     */
    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    /**
     * Check if a JsonObject contains a key and return its value.
     * @param obj The Json instance.
     * @param key The key to check.
     * @return Returns the value associated with the key.
     */
    private String optString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
            ? obj.get(key).getAsString()
            : null;
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
