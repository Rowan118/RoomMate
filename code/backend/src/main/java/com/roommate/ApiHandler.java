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

/** Routes /api/register and /api/login. */

//Ila -- added route to where preferences will be handled
//actual names can be changed - just make sure to change them in the frontend too

/**
 * Handle API requests
 * ApiHandler
 */
public class ApiHandler implements HttpHandler {

    private static final Gson GSON = new Gson();

    private final UserDao users;

    /**
     * ApiHandler object
     * @param users A UserDao (Data Access Object) object
     */
    public ApiHandler(UserDao users) {
        this.users = users;
    }

    /* Overridden HTTP network method from the standard library.
     * Connects the database to the http server.
     */
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
                //for adding preferences
                case "/api/addPreferences" -> handleAddPreferences(exchange);
                //for modifying preferences
                case "/api/modifyPreferences" -> handleModifyPreferences(exchange);
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

    /**
     * Handles POST /api/register
     * @param exchange HttpExchange
     * @throws IOException If a network error occurs, an IOException may be thrown.
     * @throws SQLException If accessing the SQL database fails, an SQLException may be thrown.
     */
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

    /**
     * Handles POST /api/login
     * @param exchange HttpExchange
     * @throws IOException If a network error occurs, an IOException may be thrown.
     * @throws SQLException If accessing the SQL database fails, an SQLException may be thrown.
     */
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

    /**
     * STUB
     * @param exchange
     * @throws IOException
     * @throws SQLException
     */
    public void handleAddPreferences(HttpExchange exchange) throws IOException, SQLException {
        return;
    }

    /**
     * STUB
     * @param exchange HttpExchange
     * @throws IOException If a network error occurs, an IOException may be thrown.
     * @throws SQLException If accessing the SQL database fails, an SQLException may be thrown.
     */
    public void handleModifyPreferences(HttpExchange exchange) throws IOException, SQLException {
        return;
    }

    /**
     * Check if the password is correctly formatted before accepting it.
     * @param password The password to check.
     * @return If the password is invalid, a String will be returned. If it's valid, null is returned.
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

        // Null means valid password
        return null;
    }

    // ---------- HTTP helpers ----------

    /**
     * Read an HttpExchange request and recieve a JSON object of the request for interpreting.
     * @param exchange HttpExchange
     * @throws IOException If a network error occurs, an IOException may be thrown.
     * @throws SQLException If accessing the SQL database fails, an SQLException may be thrown.
     */
    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (raw.isBlank()) {
                return new JsonObject();
            }
            return JsonParser.parseString(raw).getAsJsonObject();
        }
    }

    /**
     * Extract a String field from a JSON object.
     * @param obj The JSON to insepct.
     * @param key The JSON field to observe.
     * @return The String value within that JSON field.
     */
    private String optString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull() ? obj.get(key).getAsString() : null;
    }

    /**
     * Writes a JSON response body with the given HTTP status code.
     * @param exchange The HTTP exchange to write to.
     * @param status The HTTP status code.
     * @param payload The JSON body to serialize to.
     * @throws IOException An IOException may be thrown if writing to the stream fails.
     */
    private void sendJson(HttpExchange exchange, int status, JsonObject payload) throws IOException {
        byte[] bytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    /**
     * Sets the CORS (Cross-Origin Resource Sharing) response headers permitting cross-origin requests from the frontend.
     * Note: The wildcard {@code *} origin is for local development and should be changed if deployed on a real website.
     * @param exchange Exchange the HTTP exchange whose response headers to modify.
     */
    private void addCorsHeaders(HttpExchange exchange) {
        Headers h = exchange.getResponseHeaders();
        // Wide open for local development. Tighten to your GitHub Pages origin
        // (and eventually your real domain) before deploying.
        h.set("Access-Control-Allow-Origin", "*");
        h.set("Access-Control-Allow-Methods", "POST, OPTIONS");
        h.set("Access-Control-Allow-Headers", "Content-Type");
        h.set("Access-Control-Max-Age", "86400");
    }

    /**
     * Factory for generating a JSON success response.
     * @param message Human-readable message summary.
     * @return The response payload.
     */
    private JsonObject success(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", true);
        o.addProperty("message", message);
        return o;
    }

    /**
     * Factory for generating a JSON error response.
     * @param message Human-readable message summary.
     * @return The response payload.
     */
    private JsonObject error(String message) {
        JsonObject o = new JsonObject();
        o.addProperty("ok", false);
        o.addProperty("message", message);
        return o;
    }
}
