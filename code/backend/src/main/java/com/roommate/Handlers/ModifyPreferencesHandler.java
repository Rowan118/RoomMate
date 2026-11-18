package com.roommate.Handlers;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * ModifyPreferencesHandler
 */
final class ModifyPreferencesHandler implements ApiEndpoint {

    /**
     * Handle the preferences API
     * @param api The API handler
     * @param exchange The http exchange request
     * @throws Exception May throw an exception when a network error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws Exception {
        JsonObject body = ApiRouter.readBody(exchange);
        ApiRouter.sendJson(exchange, 501, ApiRouter.error("handleModifyPreferences recieved:\n" + body));
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPath() {
        return "/api/modifyPreferences";
    }

}
