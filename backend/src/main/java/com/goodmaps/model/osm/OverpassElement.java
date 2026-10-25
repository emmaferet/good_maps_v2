package com.goodmaps.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassElement(
        String type,
        Long id,
        Double lat,
        Double lon,
        Center center,
        Map<String, String> tags
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Center(Double lat, Double lon) {}

    public Double getEffectiveLat() {
        if (lat != null) return lat;
        if (center != null) return center.lat();
        return null;
    }

    public Double getEffectiveLon() {
        if (lon != null) return lon;
        if (center != null) return center.lon();
        return null;
    }

    public String getTag(String key) {
        return tags != null ? tags.get(key) : null;
    }
}
