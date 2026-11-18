package com.roommate.Handlers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

/**
 * Serves a JSON configuration file from the CONF_DIR.
 */
public class ConfigHandler implements ApiRoute {
    private final String filename;

    public ConfigHandler(String filename) {
        this.filename = filename;
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public void handle(HttpExchange exchange) throws Exception {
        String confDir = System.getenv().getOrDefault("CONF_DIR", "configuration");
        Path configFilePath = Paths.get(confDir, filename);
        
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        
        byte[] bytes = Files.readAllBytes(configFilePath);
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}