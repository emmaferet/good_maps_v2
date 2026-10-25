package com.goodmaps.dto;

import com.goodmaps.enums.AccessibilityProfile;
import com.goodmaps.enums.ActivityCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SearchQueryDto(
        @NotNull(message = "La latitude est obligatoire")
        @DecimalMin(value = "-90.0", message = "La latitude minimale est -90.0")
        @DecimalMax(value = "90.0", message = "La latitude maximale est 90.0")
        Double latitude,

        @NotNull(message = "La longitude est obligatoire")
        @DecimalMin(value = "-180.0", message = "La longitude minimale est -180.0")
        @DecimalMax(value = "180.0", message = "La longitude maximale est 180.0")
        Double longitude,

        @Min(value = 100, message = "Le rayon minimal est de 100 mètres")
        @Max(value = 50000, message = "Le rayon maximal est de 50 000 mètres (50km)")
        Integer radius,

        AccessibilityProfile profile,

        ActivityCategory category,

        @Min(value = 1, message = "La limite minimale est 1")
        @Max(value = 100, message = "La limite maximale est 100")
        Integer limit
) {
    public SearchQueryDto {
        if (radius == null) {
            radius = 3000; // Rayon par défaut : 3 km
        }
        if (profile == null) {
            profile = AccessibilityProfile.ALL;
        }
        if (category == null) {
            category = ActivityCategory.ALL;
        }
        if (limit == null) {
            limit = 40;
        }
    }
}
