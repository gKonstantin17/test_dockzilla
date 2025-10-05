package org.dock.entity;

import lombok.Data;

@Data
public class WeatherResponse {
    private double latitude;
    private double longitude;
    private String timezone;
    private WeatherData hourly;
}
