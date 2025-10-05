package org.dock.entity;

import lombok.Data;

import java.util.List;

@Data
public class WeatherData {
    private List<String> time;
    private List<Double> temperature_2m;
}
