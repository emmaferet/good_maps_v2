package com.goodmaps.dto;

import com.goodmaps.enums.ActivityCategory;

/**
 * DTO central représentant une activité ou un lieu accessible sur Good Maps.
 */
public record ActivityDto(
        String id,
        String name,
        ActivityCategory category,
        Double latitude,
        Double longitude,
        Double distanceMeters,
        String openingHours,
        Boolean openNow,
        String description,
        AccessibilityDetailsDto accessibility,
        ContactDto contact,
        int score,
        boolean recommended,
        String osmUrl
) {
}
