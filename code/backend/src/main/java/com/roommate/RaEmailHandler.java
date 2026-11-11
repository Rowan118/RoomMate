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
    //class to handle checking whether an email is in the list of valid emails for RA accounts
    //As we have no other way to check whether someone is an RA, these are just going to be hardcoded emails in a .json
    //For testing, clear the database and use one of the emails in emailList.json

    //same code we have in every API request...
    void handle(ApiHandler api, HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        //Read the email, check whether it is not null
        JsonObject body = ApiHandler.readBody(exchange);
        if (!body.has("email") || !body.get("email").isJsonPrimitive()) {
            api.sendJson(exchange, 400, api.error("Email is required"));
            return;
        }

        String email = body.get("email").getAsString().trim().toLowerCase(Locale.ROOT);
        Path listPath = Path.of("src", "main", "java", "com", "roommate", "emailList.json");

        //Read emailList.json and compare to the given email. Return true if email is in the list
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

            //send boolean response back to the frontend for credential database submission or error handling
            api.sendJson(exchange, 200, new JsonPrimitive(valid));
        }
    }
}