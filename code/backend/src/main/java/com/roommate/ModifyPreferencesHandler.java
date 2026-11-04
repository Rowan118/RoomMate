package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

final class ModifyPreferencesHandler {

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use GET for this endpoint"));
            return;
        }

        JsonObject body = readBody(exchange);
        api.sendJson(exchange, 501, api.error("handleModifyPreferences recieved:\n" + body));
    }

    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }
}
