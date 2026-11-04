package com.roommate;

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
    }
}
