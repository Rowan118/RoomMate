package com.roommate.Handlers;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public final class StaticFileHandler implements HttpHandler{
    
        public static final Map<String, String> TYPES = Map.of(
            ".html", "text/html; charset=utf-8",
            ".css", "text/css; charset=utf-8",
            ".js", "application/javascript; charset=utf-8",
            ".json", "application/json; charset=utf-8",
            ".png", "image/png",
            ".jpg", "image/jpeg",
            ".svg", "image/svg+xml",
            ".ico", "image/x-icon");
        private final Path root;

        public StaticFileHandler(String dir) {
            this.root = Paths.get(dir).toAbsolutePath().normalize();
        }

        @Override 
        public void handle(HttpExchange exchange) throws IOException{
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/")){
                exchange.getResponseHeaders().set("Location", "/login/login.html");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }

            Path file = root.resolve(path.substring(1)).normalize();

            if (!file.startsWith(root) || !Files.isRegularFile(file)){
                exchange.sendResponseHeaders(404, -1);
                exchange.close();
                return;
            }

            String name = file.getFileName().toString();
            String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
            byte[] bytes = Files.readAllBytes(file);
            exchange.getResponseHeaders().set("Content-Type", TYPES.getOrDefault(ext, "application/octet-stream"));
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream out = exchange.getResponseBody()){
                out.write(bytes);
            }
        }
}
