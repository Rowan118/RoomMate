package com.roommate;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String loginDbPath = System.getenv().getOrDefault("DB_PATH", "roommate.db");
        String preferenceDbPath = System.getenv().getOrDefault("DB_PATH", "preferences.db");

        // Initialize databases
        Database loginDb = new LoginDatabase(loginDbPath);
        loginDb.init();
        Database preferenceDb = new PreferencesDatabase(preferenceDbPath);
        preferenceDb.init();

        // Initialize services
        PasswordService passwords = new PasswordService();
        UserDao users = new UserDao(loginDb, passwords);
        SessionStore sessions = new SessionStore();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler(users, sessions));

        // Single-threaded: SQLite only allows one writer at a time, and for a
        // proof of concept this avoids SQLITE_BUSY entirely. Swap for a thread
        // pool once you move to PostgreSQL/MySQL.
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        System.out.println("RoomMate backend listening on http://localhost:" + port);
        System.out.println("Login SQLite file: " + loginDbPath);
        System.out.println("Preferences SQLite file: " + preferenceDbPath);
    }
}
