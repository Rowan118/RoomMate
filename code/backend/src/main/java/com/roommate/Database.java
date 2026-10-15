package com.roommate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Owns the SQLite connection string and creates the schema on startup. */
public class Database {

    private final String url;

    public Database(String filePath) {
        // busy_timeout keeps concurrent writers from failing instantly with SQLITE_BUSY
        this.url = "jdbc:sqlite:" + filePath + "?busy_timeout=5000";
        init();
    }

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

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
