package com.roommate.Handlers;

import com.google.gson.JsonObject;
import com.roommate.Services.SessionStore;
import com.roommate.Services.UserDao;
import com.sun.net.httpserver.HttpExchange;

public class GetAccountTypeHandler implements ApiEndpoint {
    private final UserDao users;
    private final SessionStore sessions;

    public GetAccountTypeHandler(UserDao users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    @Override
    public void handle(HttpExchange exchange) throws Exception {
        Long userId = ApiRouter.getAuthenticatedUserId(exchange, sessions, users);
        if (userId == null) return;

        String accountType = sessions.getAccountType(userId.toString());
        if (accountType == null) {
            ApiRouter.sendJson(exchange, 404, ApiRouter.error("Could not find account type for this user."));
            return;
        }

        JsonObject response = ApiRouter.success("Account type retrieved");
        JsonObject accountTypeResult = new JsonObject();
        accountTypeResult.addProperty("account_type", accountType);
        response.add("account_type", accountTypeResult);
        ApiRouter.sendJson(exchange, 200, response);
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return "/api/GetAccountType";
    }
    
}
