package com.roommate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LoginDatabase extends Database {

    public LoginDatabase(String filePath) {
        super(filePath);
    }

    @Override
    void init() {
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
}
