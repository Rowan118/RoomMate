package com.roommate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** All SQL for the users table lives here. */
public class UserDao {

    private final Database db;
    private final PasswordService passwords;

    public UserDao(Database db, PasswordService passwords) {
        this.db = db;
        this.passwords = passwords;
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void createUser(String email, String rawPassword) throws SQLException {
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            ps.setString(2, passwords.hash(rawPassword));
            ps.executeUpdate();
        }
    }

    /** @return true if the email exists and the password matches. */
    public boolean credentialsMatch(String email, String rawPassword) throws SQLException {
        String sql = "SELECT password FROM users WHERE email = ?";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false; // unknown email
                }
                return passwords.verify(rawPassword, rs.getString("password"));
            }
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
