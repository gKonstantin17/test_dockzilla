package org.dock.service;

import com.google.gson.Gson;
import org.dock.entity.GeocodingResponse;
import org.dock.entity.LocationData;
import org.dock.utils.HttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GeocodingService {
    private static final String GEOCODING_API_URL = "https://geocoding-api.open-meteo.com/v1/search?name=";
    private final Gson gson = new Gson();

    public LocationData getCoordinates(String city) throws IOException, InterruptedException {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = GEOCODING_API_URL + encodedCity;

        String response = HttpClient.get(url);
        GeocodingResponse geocodingResponse = gson.fromJson(response, GeocodingResponse.class);

        if (geocodingResponse.getResults() == null || geocodingResponse.getResults().isEmpty()) {
            throw new IllegalArgumentException("City not found: " + city);
        }

        return geocodingResponse.getResults().get(0);
    }
}
