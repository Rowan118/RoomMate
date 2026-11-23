package com.roommate.Handlers;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.roommate.Services.UserDao;
import com.sun.net.httpserver.HttpExchange;

/**
 * RegisterHandler
 */
final class RegisterHandler implements ApiEndpoint {

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
        JsonObject body = ApiRouter.readBody(exchange);
        String email = ApiRouter.optString(body, "email");
        String password = ApiRouter.optString(body, "password");
        String accountType = ApiRouter.optString(body, "account");

        if (accountType == null || accountType.toLowerCase().equals("none")) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error("Please select an account type"));
            return;
        }

        if (email == null || !email.toLowerCase().endsWith("@wsu.edu")) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error("Not a valid WSU email"));
            return;
        }

        String passwordProblem = validatePassword(password);
        if (passwordProblem != null) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error(passwordProblem));
            return;
        }

        if (users.emailExists(email)) {
            ApiRouter.sendJson(exchange, 409, ApiRouter.error("An account with that email already exists"));
            return;
        }

        try {
            users.createUser(email, password, accountType);
        } catch (SQLException e) {
            // The unique constraint may fire if another request registers this email concurrently.
            ApiRouter.sendJson(exchange, 409, ApiRouter.error("An account with that email already exists"));
            return;
        }

        ApiRouter.sendJson(exchange, 201, ApiRouter.success("Account created"));
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

    @Override 
    public String getPath() {
        return "/api/register";
    }

}
