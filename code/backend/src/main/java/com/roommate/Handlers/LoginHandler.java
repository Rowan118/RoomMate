package com.roommate.Handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * LoginHandler
 */
final class LoginHandler implements ApiEndpoint {

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
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        JsonObject body = readBody(exchange);
        String email = optString(body, "email");
        String password = optString(body, "password");

        if (email == null || password == null) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error("Email and password are required"));
            return;
        }

        if (!users.credentialsMatch(email, password)) {
            ApiRouter.sendJson(exchange, 401, ApiRouter.error("Invalid email or password"));
            return;
        }

        sessions.removeSession(ApiRouter.readCookie(exchange, "sessionId"));
        String sessionId = sessions.createSession(email);
        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax"
        );

        ApiRouter.sendJson(exchange, 200, ApiRouter.success("Logged in"));
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

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/api/login";
    }
}
