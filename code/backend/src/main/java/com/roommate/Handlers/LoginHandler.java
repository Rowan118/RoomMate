package com.roommate.Handlers;

import com.google.gson.JsonObject;
import com.roommate.SessionStore;
import com.roommate.UserDao;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * LoginHandler
 */
final class LoginHandler implements ApiEndpoint {

    private final UserDao users;
    private final SessionStore sessions;

    /**
     * Create a new LoginHandler instance.
     * @param users The UserDao of users
     * @param sessions A SessionStore of sessions
     */
    LoginHandler(UserDao users, SessionStore sessions) {
        this.users = users;
        this.sessions = sessions;
    }

    /**
     * Handle login API requests.
     * @param api The APIHandler instance
     * @param exchange The http request
     * @throws Exception May throw an exception if an unexpected network error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        JsonObject body = ApiRouter.readBody(exchange);
        String email = ApiRouter.optString(body, "email");
        String password = ApiRouter.optString(body, "password");

        if (email == null || password == null) {
            ApiRouter.sendJson(exchange, 400, ApiRouter.error("Email and password are required"));
            return;
        }

        if (!users.credentialsMatch(email, password)) {
            ApiRouter.sendJson(exchange, 401, ApiRouter.error("Invalid email or password"));
            return;
        }

        sessions.removeSession(ApiRouter.readCookie(exchange, "sessionId"));
        String sessionId = sessions.createSession(email);
        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=" + sessionId + "; Path=/; HttpOnly; SameSite=Lax"
        );

        ApiRouter.sendJson(exchange, 200, ApiRouter.success("Logged in"));
    }


    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/api/login";
    }
}
