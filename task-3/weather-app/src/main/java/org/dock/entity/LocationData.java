package org.dock.entity;

import lombok.Data;

@Data
public class LocationData {
    private String name;
    private double latitude;
    private double longitude;
    private String country;
}
