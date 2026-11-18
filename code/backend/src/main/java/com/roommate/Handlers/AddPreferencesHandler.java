package com.roommate.Handlers;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.roommate.Services.PreferencesService;
import com.sun.net.httpserver.HttpExchange;

public class AddPreferencesHandler implements ApiRoute {

    private final UserDao users;
    private final SessionStore sessions;
    private final PreferencesService preferencesService;

    public AddPreferencesHandler(UserDao users, SessionStore sessions, PreferencesService preferencesService) {
        this.users = users;
        this.sessions = sessions;
        this.preferencesService = preferencesService;
    }

    @Override
    public void handle(HttpExchange exchange) throws Exception {
        String email = sessions.getEmail(ApiHandler.readCookie(exchange, "sessionId"));
        if (email == null) {
            ApiHandler.sendJson(exchange, 401, ApiHandler.error("Not logged in"));
            return;
        }

        Long userId = users.findUserIdByEmail(email);
        if (userId == null) {
            ApiHandler.sendJson(exchange, 401, ApiHandler.error("Not logged in"));
            return;
        }

        JsonObject body = ApiHandler.readBody(exchange);
        if (body.size() == 0) {
            ApiHandler.sendJson(exchange, 400, ApiHandler.error("No preferences provided"));
            return;
        }

        try {
            preferencesService.save(userId, body);
        } catch (SQLException e) {
            e.printStackTrace();
            ApiHandler.sendJson(exchange, 500, ApiHandler.error("Failed to save preferences. They may already exist."));
            return;
        }

        JsonObject response = ApiHandler.success("Preferences saved");
        response.add("preferences", body);
        ApiHandler.sendJson(exchange, 200, response);
    }

    @Override
    public String getMethod() {
        return "POST";
    }

}