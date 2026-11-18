package com.roommate.Handlers;

import com.roommate.SessionStore;
import com.sun.net.httpserver.HttpExchange;

/**
 * LogoutHandler
 */
final class LogoutHandler implements ApiRoute {

    private final SessionStore sessions;

    /**
     * Create a LogoutHandler instance
     * @param sessions SessionStore of current sessions.
     */
    LogoutHandler(SessionStore sessions) {
        this.sessions = sessions;
    }

    /**
     * Handle API requests regarding logging out.
     * @param api APIHandler instance
     * @param exchange The http request
     * @throws Exception May throw an exception if an unexpected network error occurs.
     */
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        String sessionId = ApiHandler.readCookie(exchange, "sessionId");
        sessions.removeSession(sessionId);

        exchange.getResponseHeaders().add(
            "Set-Cookie",
            "sessionId=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"
        );

        ApiHandler.sendJson(exchange, 200, ApiHandler.success("Logged out"));
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/api/logout";
    }

}
