package com.roommate.Services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public JsonObject getPreferences(long userId) throws SQLException {
        String sql = "SELECT * FROM student_preferences WHERE user_id = ?";
        JsonObject schema = preferences.getSchema();
        JsonObject result = new JsonObject();

        try (Connection conn = preferences.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setLong(1, userId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Loop through the schema keys to fetch the exact columns we expect
                    for (String key : schema.keySet()) {
                        String type = schema.getAsJsonObject(key).has("type") 
                            ? schema.getAsJsonObject(key).get("type").getAsString() 
                            : "text";

                        if ("boolean".equals(type)) {
                            // Convert SQLite 1/0 back to JSON true/false
                            result.addProperty(key, rs.getInt(key) == 1);
                        } else {
                            result.addProperty(key, rs.getString(key));
                        }
                    }
                    return result;
                }
            }
        }
        return null; // Returns null if the user hasn't filled out their preferences yet
    }
}