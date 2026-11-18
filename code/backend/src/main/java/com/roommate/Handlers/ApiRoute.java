package com.roommate.Handlers;

import com.sun.net.httpserver.HttpExchange;

/**
 * Defines a specific API endpoint.
 */
public interface ApiRoute {
    
    /**
     * Get the required HTTP method.
     * @return The HTTP method required (e.g., "POST", "GET").
     */
    String getMethod();

    /**
     * Handles the HTTP request.
     * @param exchange 
     * @throws Exception
     */
    void handle(HttpExchange exchange) throws Exception;
}
