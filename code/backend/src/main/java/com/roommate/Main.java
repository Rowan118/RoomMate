package com.roommate;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.roommate.Databases.Database;
import com.roommate.Databases.LoginDatabase;
import com.roommate.Databases.PreferencesDatabase;
import com.roommate.Handlers.ApiRouter;
import com.sun.net.httpserver.HttpServer;

import com.roommate.Services.PasswordService;
import com.roommate.Services.PreferencesService;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String loginDbPath = System.getenv().getOrDefault("LOGIN_DB", "logins.db");
        String preferenceDbPath = System.getenv().getOrDefault("PREF_DB", "preferences.db");
        String configurationPath = System.getenv().getOrDefault("CONF_DIR", "configuration");

        JsonObject preferencesSettings;
        try {
            // Construct the path to the JSON file
            String preferencesFilePath = Paths.get(configurationPath, "preferences.json").toString();
            
            // Read the file into a String
            String jsonContent = Files.readString(Paths.get(preferencesFilePath), StandardCharsets.UTF_8);
            
            // Parse the String into a JsonObject using Gson
            preferencesSettings = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            System.out.println("Successfully loaded preferences schema from: " + preferencesFilePath);
        } catch (Exception e) {
            System.err.println("Failed to retrieve or parse preferences settings file: " + e.getMessage());
            System.err.println("Exiting backend...");
            System.exit(1);
            return;
        }


        // Debug path prints
        System.out.println("cwd: " + System.getenv().get("PWD") +
            "\nconfig path: " + configurationPath + 
            "\nlogin database path: " + loginDbPath + 
            "\npreferences database path: " + preferenceDbPath);

        // Initialize databases
        Database loginDb = new LoginDatabase(loginDbPath);
        loginDb.init();
        PreferencesDatabase preferenceDb = new PreferencesDatabase(preferenceDbPath, preferencesSettings);
        preferenceDb.init();

        // Initialize services
        PasswordService passwords = new PasswordService();
        PreferencesService preferences = new PreferencesService(preferenceDb);
        UserDao users = new UserDao(loginDb, passwords);
        SessionStore sessions = new SessionStore();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api", new ApiRouter(users, sessions, preferences));

        // Single-threaded: SQLite only allows one writer at a time
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        System.out.println("RoomMate backend listening on http://localhost:" + port);
        System.out.println("Login SQLite file: " + loginDbPath);
        System.out.println("Preferences SQLite file: " + preferenceDbPath);
    }
}
