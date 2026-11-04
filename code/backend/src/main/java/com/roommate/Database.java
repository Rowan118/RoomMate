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
public abstract class Database {

    private final String url;

    /**
     * Create a Database instance of an SQLite database.
     * @param filePath Path to the .db SQLite file.
     */
    public Database(String filePath) {
        // busy_timeout keeps concurrent writers from failing instantly with SQLITE_BUSY
        this.url = "jdbc:sqlite:" + filePath + "?busy_timeout=5000";
    }

    /**
     * Create a new database prepopulated with columns.
     */
    abstract void init();

    /**
     * Attempt to connect to the SQLite database.
     * @return A Connection object.
     * @throws SQLException May throw an SQLException.
     */
    public Connection connect() throws SQLException {
        return DriverManager.getConnection(url);
    }
}
