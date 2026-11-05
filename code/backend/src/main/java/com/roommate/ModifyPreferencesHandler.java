package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

/**
 * 
 * ModifyPreferencesHandler
 */
final class ModifyPreferencesHandler {


    /**
     * Handle the preferences API
     * @param api The API handler
     * @param exchange The http exchange request
     * @throws Exception May throw an exception when a network error occurs
     */
    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use GET for this endpoint"));
            return;
        }

        JsonObject body = ApiHandler.readBody(exchange);
        api.sendJson(exchange, 501, api.error("handleModifyPreferences recieved:\n" + body));
    }
}
