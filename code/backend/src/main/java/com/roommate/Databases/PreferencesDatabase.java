package com.roommate.Databases;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * PreferencesDatabase
 */
public class PreferencesDatabase extends Database {
    
    private JsonObject schema;

    /**
     * Create a new student preferences SQLite database.
     * Contains the user's e-mail along with a plethora of user preferences for which dorm to be selected for.
     * @param filePath Path to the SQLite database file.
     */
    public PreferencesDatabase(String filePath, JsonObject schema) {
        super(filePath);
        this.schema = schema;
    }

    /**    (non-Javadoc)
     * 
     * @see com.roommate.Databases.Database#init()
     */
    @Override
    public 
    void init() {
                // Build the SQL string dynamically based on the JSON
        String createTableSql = buildCreateTableSql();

        // Optional: You can keep triggers/indexes as hardcoded strings if they are static
        String createTriggerSql = """
            CREATE TRIGGER IF NOT EXISTS lock_placement
            BEFORE UPDATE OF dorm, gender, room_type ON student_preferences
            WHEN OLD.dorm IS NOT NEW.dorm
                OR OLD.gender IS NOT new.gender
                OR OLD.room_type IS NOT new.room_type
            BEGIN
                SELECT RAISE(ABORT, 'Placement profile is locked');
            END
            """;

        String[] sqlStatements = {
            createTableSql,
            createTriggerSql
        };

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            for (String statement : sqlStatements) {
                st.execute(statement);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database: " + e.getMessage(), e);
        }
    }

    public JsonObject getSchema() {
        return schema;
    }

    /**
     * Build an SQL schema for the preferences database based on the settings file's configuration.
     * @return Formatted SQL Create Table String.
     */
    private String buildCreateTableSql() {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS student_preferences (\n");

        // Every user needs a unique ID referencing the users table
        sql.append("  user_id INTEGER PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,\n");

        // Loop through every key in preferences.json
        for (Map.Entry<String, JsonElement> entry : schema.entrySet()) {
            String columnName = entry.getKey();
            JsonObject config = entry.getValue().getAsJsonObject();
            
            String type = config.has("type") ? config.get("type").getAsString() : "text";
            boolean required = config.has("required") && config.get("required").getAsBoolean();

            sql.append("  ").append(columnName).append(" ");

            // Convert JSON types to SQLite types
            switch (type) {
                case "boolean":
                    // SQLite uses 1 and 0 for booleans
                    sql.append("INTEGER"); 
                    break;
                case "number":
                    // Or REAL if you need decimals
                    sql.append("INTEGER");
                    break;
                case "text":
                case "select":
                default:
                    sql.append("TEXT");
                    break;
            }

            // Add NOT NULL if required
            if (required) {
                sql.append(" NOT NULL");
            }

            // Add CHECK constraints for dropdowns (select)
            if (type.equals("select") && config.has("options")) {
                JsonArray options = config.getAsJsonArray("options");
                sql.append(" CHECK (").append(columnName).append(" IN (");
                for (int i = 0; i < options.size(); i++) {
                    sql.append("'").append(options.get(i).getAsString()).append("'");
                    if (i < options.size() - 1) sql.append(", ");
                }
                sql.append("))");
            }

            // Add CHECK constraint for booleans
            if (type.equals("boolean")) {
                sql.append(" CHECK (").append(columnName).append(" IN (0, 1))");
            }

            sql.append(",\n");
        }

        // Add the updated_at column at the end
        sql.append("  updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP\n");
        sql.append(")");

        System.out.println("Generated SQL:\n" + sql.toString());
        return sql.toString();
    }
}
