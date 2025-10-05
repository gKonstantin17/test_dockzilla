package org.dock.entity;

import lombok.Data;

import java.util.List;

@Data
public class GeocodingResponse {
    private List<LocationData> results;
}
