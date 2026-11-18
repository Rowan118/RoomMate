package com.roommate.Handlers;

import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.roommate.Services.PreferencesService;
import com.sun.net.httpserver.HttpExchange;

public class AddPreferencesHandler implements ApiEndpoint {

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
        Long userId = ApiRouter.getAuthenticatedUserId(exchange, sessions, users);
        if (userId == null) return;

        JsonObject body = ApiRouter.readBody(exchange);
        if (body.size() == 0) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error("No preferences provided"));
            return;
        }

        try {
            preferencesService.save(userId, body);
        } catch (SQLException e) {
            e.printStackTrace();
            ApiRouter.sendJson(exchange, 500, ApiRouter.error("Failed to save preferences. They may already exist."));
            return;
        }

        JsonObject response = ApiRouter.success("Preferences saved");
        response.add("preferences", body);
        ApiRouter.sendJson(exchange, 200, response);
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/api/addPreferences";
    }

}