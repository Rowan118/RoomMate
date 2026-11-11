package com.roommate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * LoginDatabase
 */
public class LoginDatabase extends Database {

    /**
     * Create a new login SQLite database.
     * Contains user e-mail, password hashes, and login attempts.
     * @param filePath Path to the SQLite database file.
     */
    public LoginDatabase(String filePath) {
        super(filePath);
    }

    /**    (non-Javadoc)
     * 
     * @see com.roommate.Database#init()
     */
    @Override
    void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                account_type    TEXT NOT NULL,
                email           TEXT NOT NULL UNIQUE COLLATE NOCASE,
                password        TEXT NOT NULL,
                created_at      TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
            """;

        try (Connection conn = connect(); Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }
}
