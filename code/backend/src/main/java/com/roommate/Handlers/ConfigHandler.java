package com.roommate.Handlers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.net.httpserver.HttpExchange;

/**
 * Serves a JSON configuration file from the CONF_DIR.
 */
public class ConfigHandler implements ApiEndpoint {
    private final String filename;
    private final String httpPath;

    public ConfigHandler(String filename, String httpPath) {
        this.filename = filename;
        this.httpPath = httpPath;
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public String getPath() {
        return httpPath;
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