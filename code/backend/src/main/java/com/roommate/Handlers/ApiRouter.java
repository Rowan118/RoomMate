package com.roommate.Handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.roommate.Services.PreferencesService;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/** Handles API requests and delegates endpoint-specific logic to separate classes. */
/**
 * Handle API requests
 * ApiHandler
 */
public class ApiRouter implements HttpHandler {

    private static final Gson GSON = new Gson();

    // Keeps track of what HTTP requests go to which handler.
    private final Map<String, ApiEndpoint> routes = new HashMap<>();


    /**
     * ApiHandler object
     * @param users A UserDao (Data Access Object) object.
     * @param sessions A session store used to track logged-in users.
     * @param preferences A database to keep track of user preferences.
     */
    public ApiRouter(UserDao users, SessionStore sessions, PreferencesService preferencesService) {
        // Add handlers to be utilized
        List<ApiEndpoint> handlerList = List.of(
            new RegisterHandler(users),
            new LoginHandler(users, sessions),
            new SessionHandler(sessions),
            new LogoutHandler(sessions),
            new RaEmailHandler(),
            new AddPreferencesHandler(users, sessions, preferencesService),
            new ModifyPreferencesHandler(),
            new ConfigHandler("preferences.json", "/api/config"),
            new ConfigHandler("dorms.json", "/api/dorms")
        );

        for (ApiEndpoint handler : handlerList) {
            routes.put(handler.getPath(), handler);
        }
    }


    /* Overridden HTTP network method from the standard library.
     * Connects the database to the http server.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        String path = exchange.getRequestURI().getPath();
        ApiEndpoint route = routes.get(path);

        if (route == null) {
            sendJson(exchange, 404, error("Unknown endpoint: " + path));
            return;
        }

        // Centralized Method Checking
        if (!route.getMethod().equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, error("Use " + route.getMethod() + " for this endpoint"));
            return;
        }

        try {
            route.handle(exchange);
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Database error"));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, error("Server error"));
        }
    }

    /**
     * Writes a JSON response body with the given HTTP status code.
     * @param exchange The HTTP exchange to write to.
     * @param status The HTTP status code.
     * @param payload The JSON body to serialize to.
     * @throws IOException An IOException may be thrown if writing to the stream fails.
     */
    static void sendJson(HttpExchange exchange, int status, JsonElement payload) throws IOException {
        byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Factory for generating a JSON success response.
     * @param message Human-readable message summary.
     * @return The response payload.
     */
    static JsonObject success(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("ok", true);
        payload.addProperty("message", message);
        return payload;
    }

    /**
     * Factory for generating a JSON error response.
     * @param message Human-readable message summary.
     * @return The response payload.
     */
    static JsonObject error(String message) {
        JsonObject payload = new JsonObject();
        payload.addProperty("ok", false);
        payload.addProperty("message", message);
        return payload;
    }

    /**
     * Sets the CORS (Cross-Origin Resource Sharing) response headers permitting cross-origin requests from the frontend.
     * Note: The wildcard {@code *} origin is for local development and should be changed if deployed on a real website.
     * @param exchange Exchange the HTTP exchange whose response headers to modify.
     */
    private void addCorsHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin != null && !origin.isBlank()) {
            headers.set("Access-Control-Allow-Origin", origin);
        } else {
            // TODO: Figure out how to handle these hardcoded addresses.
            headers.set("Access-Control-Allow-Origin", "http://localhost:8000");
        }

        headers.set("Access-Control-Allow-Credentials", "true");
        headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Access-Control-Max-Age", "86400");
    }

    /**
     * Read an HttpExchange request and recieve a JSON object of the request for interpreting.
     * @param exchange HttpExchange
     * @throws IOException If a network error occurs, an IOException may be thrown.
     * @throws SQLException If accessing the SQL database fails, an SQLException may be thrown.
     */
    static JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    /**
     * Get a cookie from an http request and a key.
     * @param exchange Http request.
     * @param key Key
     * @return Cookie String
     */
    static protected String readCookie(HttpExchange exchange, String key) {
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
