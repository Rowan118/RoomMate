package com.roommate;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/** Routes /api/register and /api/login. */
public class ApiHandler implements HttpHandler {

    private static final Gson GSON = new Gson();

    private final UserDao users;

    public ApiHandler(UserDao users) {
        this.users = users;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        // Browsers send a preflight OPTIONS request before the real POST.
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            switch (path) {
                case "/api/register" -> handleRegister(exchange);
                case "/api/login"    -> handleLogin(exchange);
                default              -> sendJson(exchange, 404, error("Unknown endpoint: " + path));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Database error"));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Server error"));
        }
    }

    // ---------- POST /api/register ----------

    private void handleRegister(HttpExchange exchange) throws IOException, SQLException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use POST for this endpoint"));
            return;
        }

        JsonObject body = readBody(exchange);
        String email = optString(body, "email");
        String password = optString(body, "password");

        if (email == null || !email.toLowerCase().endsWith("@wsu.edu")) {
            sendJson(exchange, 400, error("Not a valid WSU email"));
            return;
        }

        String passwordProblem = validatePassword(password);
        if (passwordProblem != null) {
            sendJson(exchange, 400, error(passwordProblem));
            return;
        }

        if (users.emailExists(email)) {
            sendJson(exchange, 409, error("An account with that email already exists"));
            return;
        }

        try {
            users.createUser(email, password);
        } catch (SQLException e) {
            // UNIQUE constraint fired because of a race between the check and the insert.
            sendJson(exchange, 409, error("An account with that email already exists"));
            return;
        }

        sendJson(exchange, 201, success("Account created"));
    }

    // ---------- POST /api/login ----------

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use POST for this endpoint"));
            return;
        }

        JsonObject body = readBody(exchange);
        String email = optString(body, "email");
        String password = optString(body, "password");

        if (email == null || password == null) {
            sendJson(exchange, 400, error("Email and password are required"));
            return;
        }

        if (!users.credentialsMatch(email, password)) {
            // Deliberately vague: don't reveal whether the email exists.
            sendJson(exchange, 401, error("Invalid email or password"));
            return;
        }

        sendJson(exchange, 200, success("Logged in"));
    }

    // ---------- Shared validation (mirrors the client-side rules) ----------

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
        return null; // valid
    }

    // ---------- HTTP helpers ----------

    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                return new JsonObject();
            }
            return JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    private String optString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    private void sendJson(HttpExchange exchange, int status, JsonObject payload) throws IOException {
        byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        // Wide open for local development. Tighten to your GitHub Pages origin
        // (and eventually your real domain) before deploying.
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");
        h.set("Access-Control-Max-Age", "86400");
    }

    private JsonObject success(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", true);
        o.addProperty("message", message);
        return o;
    }

    private JsonObject error(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("message", message);
        return o;
    }
}
