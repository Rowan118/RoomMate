package com.roommate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Create an abstraction between the user credentials database to separate database management from server logic*/
public class UserDao {

    private final Database db;
    private final PasswordService passwords;

    /**
     * Create a UserDao instance that abstracts the PasswordService and Database away.
     * @param db Database object.
     * @param passwords PasswordService object.
     */
    public UserDao(Database db, PasswordService passwords) {
        this.db = db;
        this.passwords = passwords;
    }

    /**
     * Check if a user's e-mail exists within the database.
     * @param email User e-mail String (eg. test@wsu.edu).
     * @return True if the e-mail address exists in the database.
     * @throws SQLException May throw an SQLException if the database unexpectedly fails.
     */
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

    public Long findUserIdByEmail(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("id") : null;
            }
        }
    }

    /**
     * Create a new user row in the database.
     * @param email The new user's e-mail address.
     * @param rawPassword The new user's raw password text (this will then be stored as a hash).
     * @throws SQLException May throw an SQLException if the database unexpectedly fails.
     */
    public void createUser(String email, String rawPassword) throws SQLException {
        String sql = "INSERT INTO users (email, password) VALUES (?, ?)";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalizeEmail(email));
            ps.setString(2, passwords.hash(rawPassword));
            ps.executeUpdate();
        }
    }

    /**
     * Check to see if a user's e-mail address exists and if their password matches.
     * @param email The user's e-mail address String.
     * @param rawPassword The password the user provides (eg. in the login screen).
     * @return True if the e-mail exists and the password matches.
     * @throws SQLException May throw an SQLException if the database unexpectedly fails.
     */
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

    /**
     * Trim e-mail address and make it case insensitive.
     * @param email E-mail String text.
     * @return Normalized e-mail address String.
     */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
