package com.roommate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Owns the SQLite connection string and creates the schema on startup. */
/**
 * Create an SQLite database connection.
 * Database
 */
public class Database {

    private final String url;

    /**
     * Create a Database instance of an SQLite database.
     * @param filePath Path to the .db SQLite file.
     */
    public Database(String filePath) {
        // busy_timeout keeps concurrent writers from failing instantly with SQLITE_BUSY
        this.url = "jdbc:sqlite:" + filePath + "?busy_timeout=5000";
        init();
    }

    /**
     * Create a new database for user accounts
     */
    private void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id         INTEGER PRIMARY KEY AUTOINCREMENT,
                email      TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password   TEXT NOT NULL,
                created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    /**
     * Attempt to connect to the SQLite database.
     * @return A Connection object.
     * @throws SQLException May throw an SQLException.
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
