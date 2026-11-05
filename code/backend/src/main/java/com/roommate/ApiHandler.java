package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Handles API requests and delegates endpoint-specific logic to separate classes. */
public class ApiHandler implements HttpHandler {

    private static final Gson GSON = new Gson();

    private final RegisterHandler registerHandler;
    private final LoginHandler loginHandler;
    private final SessionHandler sessionHandler;
    private final LogoutHandler logoutHandler;
    private final AddPreferencesHandler addPreferencesHandler;
    private final ModifyPreferencesHandler modifyPreferencesHandler;

    public ApiHandler(UserDao users, SessionStore sessions) {
        this.registerHandler = new RegisterHandler(users);
        this.loginHandler = new LoginHandler(users, sessions);
        this.sessionHandler = new SessionHandler(sessions);
        this.logoutHandler = new LogoutHandler(sessions);
        this.addPreferencesHandler = new AddPreferencesHandler(sessions);
        this.modifyPreferencesHandler = new ModifyPreferencesHandler(sessions);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        try {
            switch (path) {
                case "/api/register" -> registerHandler.handle(this, exchange);
                case "/api/login" -> loginHandler.handle(this, exchange);
                case "/api/session" -> sessionHandler.handle(this, exchange);
                case "/api/logout" -> logoutHandler.handle(this, exchange);
                case "/api/addPreferences" -> addPreferencesHandler.handle(this, exchange);
                case "/api/modifyPreferences" -> modifyPreferencesHandler.handle(this, exchange);
                default -> sendJson(exchange, 404, error("Unknown endpoint: " + path));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Database error"));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Server error"));
        }
    }

    void sendJson(HttpExchange exchange, int status, JsonObject payload) throws IOException {
        byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    JsonObject success(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("ok", true);
        payload.addProperty("message", message);
        return payload;
    }

    JsonObject error(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("ok", false);
        payload.addProperty("message", message);
        return payload;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin != null && !origin.isBlank()) {
            headers.set("Access-Control-Allow-Origin", origin);
        } else {
            headers.set("Access-Control-Allow-Origin", "http://localhost:8000");
        }

        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Access-Control-Max-Age", "86400");
    }

    static String readCookie(HttpExchange exchange, String key) {
        Headers headers = exchange.getRequestHeaders();
        String cookieHeader = headers.getFirst("Cookie");
        if (cookieHeader == null) return null;

        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(key + "=")) {
                return trimmed.substring((key + "=").length());
            }
        }
        return null;
    }

    static JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }
}
