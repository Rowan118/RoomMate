package com.roommate;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String dbPath = System.getenv().getOrDefault("DB_PATH", "roommate.db");

        Database db = new Database(dbPath);
        PasswordService passwords = new PasswordService();
        UserDao users = new UserDao(db, passwords);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiHandler(users));

        // Single-threaded: SQLite only allows one writer at a time, and for a
        // proof of concept this avoids SQLITE_BUSY entirely. Swap for a thread
        // pool once you move to PostgreSQL/MySQL.
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        System.out.println("RoomMate backend listening on http://localhost:" + port);
        System.out.println("SQLite file: " + dbPath);
    }
}
