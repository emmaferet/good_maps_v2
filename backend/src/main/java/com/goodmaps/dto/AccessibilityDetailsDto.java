package com.goodmaps.dto;

import java.util.Map;

/**
 * Détails normalisés des critères d'accessibilité selon les tags OpenStreetMap.
 */
public record AccessibilityDetailsDto(
        String wheelchair, // "yes", "limited", "no", "unknown"
        String wheelchairDescription,
        Boolean toiletsWheelchair,
        Boolean tactilePaving,
        Boolean hearingLoop,
        Boolean braille,
        String blindDescription,
        String deafDescription,
        String levelAccess,
        Map<String, String> rawTags
) {
    public static AccessibilityDetailsDto empty() {
        return new AccessibilityDetailsDto(
                "unknown",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Map.of()
        );
    }
}
