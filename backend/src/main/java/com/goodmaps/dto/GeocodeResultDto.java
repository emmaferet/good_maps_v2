package com.goodmaps.dto;

import java.util.List;

/**
 * Résultat normalisé de géocodage textuel issu de Nominatim.
 */
public record GeocodeResultDto(
        String displayName,
        Double latitude,
        Double longitude,
        String type,
        List<Double> boundingBox
) {
}
