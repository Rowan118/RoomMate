package com.roommate.Handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.roommate.UserDao;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * RegisterHandler
 */
final class RegisterHandler implements ApiRoute {

    private final UserDao users;

    /**
     * A RegisterHandler instance
     * @param users UserDao of users
     */
    RegisterHandler(UserDao users) {
        this.users = users;
    }

    /**
     * Handle API requests regarding signing up.
     * @param api ApiHandler instance
     * @param exchange Http request
     * @throws Exception May throw an exception if an unexpected network error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        JsonObject body = ApiHandler.readBody(exchange);
        String email = optString(body, "email");
        String password = optString(body, "password");
        String accountType = optString(body, "account");

        if (accountType == null || accountType.toLowerCase().equals("none")) {
            ApiHandler.sendJson(exchange, 400, ApiHandler.error("Please select an account type"));
            return;
        }

        if (email == null || !email.toLowerCase().endsWith("@wsu.edu")) {
            ApiHandler.sendJson(exchange, 400, ApiHandler.error("Not a valid WSU email"));
            return;
        }

        String passwordProblem = validatePassword(password);
        if (passwordProblem != null) {
            ApiHandler.sendJson(exchange, 400, ApiHandler.error(passwordProblem));
            return;
        }

        if (users.emailExists(email)) {
            ApiHandler.sendJson(exchange, 409, ApiHandler.error("An account with that email already exists"));
            return;
        }

        try {
            users.createUser(email, password, accountType);
        } catch (SQLException e) {
            // The unique constraint may fire if another request registers this email concurrently.
            ApiHandler.sendJson(exchange, 409, ApiHandler.error("An account with that email already exists"));
            return;
        }

        ApiHandler.sendJson(exchange, 201, ApiHandler.success("Account created"));
    }

    private String optString(JsonObject obj, String key) {
        return obj.has(key) && !obj.get(key).isJsonNull()
            ? obj.get(key).getAsString()
            : null;
    }

    /**
     * Check if the incoming password is valid.
     * @param password Raw password String.
     * @return True if it's a valid password.
     */
    private String validatePassword(String password) {
        if (password == null || password.length() < 10) {
            return "Password must be at least 10 characters long";
        }
        if (password.equals("Securepassword3")) {
            return "Using the example as your password is not allowed";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one capital letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number";
        }
        return null;
    }

    @Override
    public String getMethod() {
        return "POST";
    }

}
