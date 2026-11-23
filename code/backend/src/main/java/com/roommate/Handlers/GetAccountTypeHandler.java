package com.roommate.Handlers;

import com.sun.net.httpserver.HttpExchange;

public class GetAccountTypeHandler implements ApiEndpoint {

    public GetAccountTypeHandler() {

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
