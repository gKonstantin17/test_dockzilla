package org.dock.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.dock.service.WeatherService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WeatherController implements HttpHandler {
    private final WeatherService weatherService = new WeatherService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> params = parseQueryParams(exchange.getRequestURI());
                String city = params.get("city");

                if (city == null || city.isEmpty()) {
                    sendResponse(exchange, 400, "City parameter is required");
                    return;
                }

                String weatherData = weatherService.getWeatherData(city);

                exchange.getResponseHeaders().set("Content-Type", "text/html");
                sendResponse(exchange, 200, weatherData);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        } catch (IllegalArgumentException e) {
            sendResponse(exchange, 404, "City not found: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getQuery();

        if (query != null && !query.isEmpty()) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    params.put(keyValue[0], keyValue[1]);
                }
            }
        }

        return params;
    }
}
