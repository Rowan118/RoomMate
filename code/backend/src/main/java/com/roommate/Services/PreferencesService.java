package com.roommate.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.roommate.Databases.PreferencesDatabase;

public class PreferencesService {
    private final PreferencesDatabase preferences;

    public PreferencesService(PreferencesDatabase preferences) {
        this.preferences = preferences;
    }

    private void writePreferences(long userId, JsonObject body, String SQLCommand) throws SQLException {
        // 1. Build the SQL dynamically
        StringBuilder sql = new StringBuilder(SQLCommand + " student_preferences (user_id");
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

    public void save(long userId, JsonObject body) throws SQLException {
        writePreferences(userId, body, "INSERT INTO");
    }

    public void overwrite(long userId, JsonObject body) throws SQLException {
        writePreferences(userId, body, "INSERT OR REPLACE INTO");
    }
}