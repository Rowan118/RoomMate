package com.roommate.Handlers;

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
