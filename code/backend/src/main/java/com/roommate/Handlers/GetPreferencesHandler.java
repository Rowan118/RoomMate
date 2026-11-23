package com.roommate.Handlers;

import com.google.gson.JsonObject;
import com.roommate.Services.PreferencesService;
import com.roommate.Services.SessionStore;
import com.roommate.Services.UserDao;
import com.sun.net.httpserver.HttpExchange;

public class GetPreferencesHandler implements ApiEndpoint {
    private final UserDao users;
    private final SessionStore sessions;
    private final PreferencesService preferencesService;

    public GetPreferencesHandler(UserDao users, SessionStore sessions, PreferencesService preferencesService) {
        this.users = users;
        this.sessions = sessions;
        this.preferencesService = preferencesService;
    }

    @Override
    public void handle(HttpExchange exchange) throws Exception {
        // Authenticate the user
        Long userId = ApiRouter.getAuthenticatedUserId(exchange, sessions, users);
        if (userId == null) return; // The helper automatically sends the 401 error

        JsonObject preferencesData = preferencesService.getPreferences(userId);

        // In case they didn't submit their preferences yet...
        if (preferencesData == null) {
            ApiRouter.sendJson(exchange, 404, ApiRouter.error("No preferences found for this user"));
            return;
        }

        JsonObject response = ApiRouter.success("Preferences retrieved");
        response.add("preferences", preferencesData);
        ApiRouter.sendJson(exchange, 200, response);
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/api/getPreferences";
    }
}