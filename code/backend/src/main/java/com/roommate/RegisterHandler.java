package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * RegisterHandler
 */
final class RegisterHandler {

    private final UserDao users;

    /**
     * A RegisterHandler instance
     * @param users UserDao of users
     */
    RegisterHandler(UserDao users) {
        this.users = users;
    }

    /**
     * Handle API requests regarding signing up.
     * @param api ApiHandler instance
     * @param exchange Http request
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
        String accountType = optString(body, "account");

        if (accountType == null || email == null || !email.toLowerCase().endsWith("@wsu.edu")) {
            api.sendJson(exchange, 400, api.error("Not a valid WSU email"));
            return;
        }

        String passwordProblem = validatePassword(password);
        if (passwordProblem != null) {
            api.sendJson(exchange, 400, api.error(passwordProblem));
            return;
        }

        if (users.emailExists(email)) {
            api.sendJson(exchange, 409, api.error("An account with that email already exists"));
            return;
        }

        try {
            users.createUser(email, password, accountType);
        } catch (SQLException e) {
            // The unique constraint may fire if another request registers this email concurrently.
            api.sendJson(exchange, 409, api.error("An account with that email already exists"));
            return;
        }

        api.sendJson(exchange, 201, api.success("Account created"));
    }

    /**
     * Read http request body and format it in json.
     * @param exchange The http request
     * @return A Json object that contains the info of the http request.
     * @throws IOException May throw an error if an unexpected error occurs parsing the http request.
     */
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

    /**
     * Check if the incoming password is valid.
     * @param password Raw password String.
     * @return True if it's a valid password.
     */
    private String validatePassword(String password) {
        if (password == null || password.length() < 10) {
            return "Password must be at least 10 characters long";
        }
        if (password.equals("Securepassword3")) {
            return "Using the example as your password is not allowed";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one capital letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number";
        }
        return null;
    }
}
