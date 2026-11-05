package com.roommate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

final class AddPreferencesHandler {

    private final UserDao users;
    private final SessionStore sessions;
    private final PreferencesDatabase preferences;

    AddPreferencesHandler(UserDao users, SessionStore sessions, PreferencesDatabase preferences) {
        this.users = users;
        this.sessions = sessions;
        this.preferences = preferences;
    }

    void handle(ApiHandler api, HttpExchange exchange) throws Exception {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            api.sendJson(exchange, 405, api.error("Use POST for this endpoint"));
            return;
        }

        String email = sessions.getEmail(readCookie(exchange, "sessionId"));
        if (email == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        Long userId = users.findUserIdByEmail(email);
        if (userId == null) {
            api.sendJson(exchange, 401, api.error("Not logged in"));
            return;
        }

        JsonObject body = readBody(exchange);
        String firstName = requiredString(body, "firstName");
        String lastName = requiredString(body, "lastName");
        String major = requiredString(body, "major");
        String dorm = requiredString(body, "dormPref");
        String roomType = requiredString(body, "roomType");
        String age = requiredString(body, "age");
        String gender = requiredString(body, "gender");
        String sameGenderPref = requiredString(body, "sameGenderPref");
        String wakeTime = requiredString(body, "wakeTime");
        String sleepTime = requiredString(body, "sleepTime");
        String alcoholPref = requiredString(body, "alcoholPref");

        if (firstName == null || lastName == null || major == null || dorm == null
                || roomType == null || age == null || gender == null || sameGenderPref == null
                || wakeTime == null || sleepTime == null || alcoholPref == null
                || !roomType.equals("single") && !roomType.equals("double")
                || !age.equals("under 19") && !age.equals("under 21") && !age.equals("21 or more")
                || !gender.equals("male") && !gender.equals("female") && !gender.equals("other")
                || !sameGenderPref.equals("yes") && !sameGenderPref.equals("no")
                || !alcoholPref.equals("under21") && !alcoholPref.equals("yes") && !alcoholPref.equals("no")) {
            api.sendJson(exchange, 400, api.error("Complete all preference fields with valid values"));
            return;
        }

        String savedAlcoholPref = alcoholPref.equals("under21") ? "under 21" : alcoholPref;
        save(userId, firstName, lastName, major, dorm, roomType, age, gender,
            sameGenderPref.equals("yes"), wakeTime, sleepTime, savedAlcoholPref);

        JsonObject savedPreferences = new JsonObject();
        savedPreferences.addProperty("firstName", firstName);
        savedPreferences.addProperty("lastName", lastName);
        savedPreferences.addProperty("major", major);
        savedPreferences.addProperty("dormPref", dorm);
        savedPreferences.addProperty("roomType", roomType);
        savedPreferences.addProperty("age", age);
        savedPreferences.addProperty("gender", gender);
        savedPreferences.addProperty("sameGenderPref", sameGenderPref);
        savedPreferences.addProperty("wakeTime", wakeTime);
        savedPreferences.addProperty("sleepTime", sleepTime);
        savedPreferences.addProperty("alcoholPref", savedAlcoholPref);

        JsonObject response = api.success("Preferences saved");
        response.add("preferences", savedPreferences);
        api.sendJson(exchange, 200, response);
    }

    private void save(long userId, String firstName, String lastName, String major, String dorm,
            String roomType, String age, String gender, boolean sameGenderPref, String wakeTime,
            String sleepTime, String alcoholPref) throws SQLException {
        try (Connection conn = preferences.connect()) {
            conn.setAutoCommit(false);
            try (PreparedStatement placement = conn.prepareStatement("""
                    INSERT INTO placement_profile
                        (user_id, first_name, last_name, major, dorm, gender, room_type, same_gender_pref, age_range)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """);
                 PreparedStatement lifestyle = conn.prepareStatement("""
                    INSERT INTO lifestyle_preferences
                        (user_id, sleep_time, wake_time, alcohol_pref)
                    VALUES (?, ?, ?, ?)
                    """)) {
                placement.setLong(1, userId);
                placement.setString(2, firstName);
                placement.setString(3, lastName);
                placement.setString(4, major);
                placement.setString(5, dorm);
                placement.setString(6, gender);
                placement.setString(7, roomType);
                placement.setInt(8, sameGenderPref ? 1 : 0);
                placement.setString(9, age);
                placement.executeUpdate();

                lifestyle.setLong(1, userId);
                lifestyle.setString(2, sleepTime);
                lifestyle.setString(3, wakeTime);
                lifestyle.setString(4, alcoholPref);
                lifestyle.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private String requiredString(JsonObject body, String key) {
        if (body == null || !body.has(key) || !body.get(key).isJsonPrimitive()
                || !body.getAsJsonPrimitive(key).isString()) {
            return null;
        }
        String value = body.get(key).getAsString().trim();
        return value.isEmpty() ? null : value;
    }

    private String readCookie(HttpExchange exchange, String key) {
        Headers headers = exchange.getRequestHeaders();
        String cookieHeader = headers.getFirst("Cookie");
        if (cookieHeader == null) {
            return null;
        }

        for (String part : cookieHeader.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(key + "=")) {
                return trimmed.substring((key + "=").length());
            }
        }
        return null;
    }

    private JsonObject readBody(HttpExchange exchange) throws IOException {
        try (InputStream in = exchange.getRequestBody()) {
            String raw = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            return raw.isBlank() ? new JsonObject() : JsonParser.parseString(raw).getAsJsonObject();
        }
    }
}
