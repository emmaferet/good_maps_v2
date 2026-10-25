package com.goodmaps.model.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OverpassResponse(
        String version,
        String generator,
        List<OverpassElement> elements
) {
    public List<OverpassElement> safeElements() {
        return elements != null ? elements : List.of();
    }
}
