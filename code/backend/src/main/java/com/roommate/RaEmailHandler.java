package com.roommate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.sun.net.httpserver.HttpExchange;

final class RaEmailHandler {
    void handle(ApiHandler api, HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        JsonObject body = ApiHandler.readBody(exchange);
        if (!body.has("email") || !body.get("email").isJsonPrimitive()) {
            api.sendJson(exchange, 400, api.error("Email is required"));
            return;
        }

        String email = body.get("email").getAsString().trim().toLowerCase(Locale.ROOT);
        Path listPath = Path.of("src", "main", "java", "com", "roommate", "emailList.json");

        try (var reader = Files.newBufferedReader(listPath, StandardCharsets.UTF_8)) {
            JsonObject list = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray entries = list.getAsJsonArray("emails");

            boolean valid = false;
            for (JsonElement entry : entries) {
                String listedEmail = entry.getAsJsonObject()
                    .get("email").getAsString().trim().toLowerCase(Locale.ROOT);
                if (listedEmail.equals(email)) {
                    valid = true;
                    break;
                }
            }

            api.sendJson(exchange, 200, new JsonPrimitive(valid));
        }
    }
}