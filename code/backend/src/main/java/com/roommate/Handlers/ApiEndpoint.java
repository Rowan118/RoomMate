package com.roommate.Handlers;

import com.sun.net.httpserver.HttpExchange;

/**
 * Defines a specific API endpoint.
 */
public interface ApiEndpoint {

    /**
     * Handles the HTTP request.
     * Any handler-specific code should live in this interface method.
     * @param exchange 
     * @throws Exception
     */
    void handle(HttpExchange exchange) throws Exception;
    
    /**
     * Get the required HTTP method that the handler should be associated with.
     * @return The HTTP method required (e.g., "POST", "GET").
     */
    String getMethod();


    /**
     * Get the associated API URL path.
     * @return URL path e.g., "/api/login"
     */
    String getPath();
}
