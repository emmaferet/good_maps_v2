package com.goodmaps.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NominatimPlace(
        @JsonProperty("place_id") Long placeId,
        @JsonProperty("lat") String lat,
        @JsonProperty("lon") String lon,
        @JsonProperty("display_name") String displayName,
        @JsonProperty("type") String type,
        @JsonProperty("boundingbox") List<String> boundingbox
) {
    public Double getLatitude() {
        return lat != null ? Double.parseDouble(lat) : null;
    }

    public Double getLongitude() {
        return lon != null ? Double.parseDouble(lon) : null;
    }
}
