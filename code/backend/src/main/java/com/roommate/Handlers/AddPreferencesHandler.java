package com.roommate.Handlers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.roommate.Databases.PreferencesDatabase;
import com.sun.net.httpserver.HttpExchange;

/**
 * AddPreferencesHandler
 */
final class AddPreferencesHandler {

    private final UserDao users;
    private final SessionStore sessions;
    private final PreferencesDatabase preferences;

    AddPreferencesHandler(UserDao users, SessionStore sessions, PreferencesDatabase preferences) {
        this.users = users;
        this.sessions = sessions;
        this.preferences = preferences;
    }

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        String email = sessions.getEmail(ApiHandler.readCookie(exchange, "sessionId"));
        if (email == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        Long userId = users.findUserIdByEmail(email);
        if (userId == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        JsonObject body = ApiHandler.readBody(exchange);
        
        // Basic check: Ensure the body isn't empty
        if (body.size() == 0) {
            api.sendJson(exchange, 400, api.error("No preferences provided"));
            return;
        }

        // Save dynamically
        try {
            save(userId, body);
        } catch (SQLException e) {
            e.printStackTrace();
            api.sendJson(exchange, 500, api.error("Failed to save preferences to database. They may already exist."));
            return;
        }

        // Send back the saved data so the frontend can display it on the submission page
        JsonObject response = api.success("Preferences saved");
        response.add("preferences", body);
        api.sendJson(exchange, 200, response);
    }

    /**
     * Dynamically builds and executes an INSERT statement based on the JSON keys.
     */
    private void save(long userId, JsonObject body) throws SQLException {
        // 1. Build the SQL dynamically
        StringBuilder sql = new StringBuilder("INSERT INTO student_preferences (user_id");
        StringBuilder placeholders = new StringBuilder(") VALUES (?");

        for (String key : body.keySet()) {
            sql.append(", ").append(key);
            placeholders.append(", ?");
        }
        sql.append(placeholders).append(")");

        // 2. Get the schema to know which fields are booleans
        JsonObject schema = preferences.getSchema();

        // 3. Execute the statement
        try (Connection conn = preferences.connect();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int index = 1;
            ps.setLong(index++, userId);

            for (Map.Entry<String, JsonElement> entry : body.entrySet()) {
                String key = entry.getKey();
                String valueString = entry.getValue().getAsString();

                // Check the schema to see if this field is a boolean
                String type = "text";
                if (schema.has(key) && schema.getAsJsonObject(key).has("type")) {
                    type = schema.getAsJsonObject(key).get("type").getAsString();
                }

                if ("boolean".equals(type)) {
                    ps.setInt(index++, valueString.equalsIgnoreCase("true") ? 1 : 0);
                } else {
                    ps.setString(index++, valueString);
                }
            }

            ps.executeUpdate();
        }
    }
}