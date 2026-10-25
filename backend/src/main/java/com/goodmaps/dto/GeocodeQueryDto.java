package com.goodmaps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record GeocodeQueryDto(
        @NotBlank(message = "Le terme de recherche géographique ne peut pas être vide")
        @Size(min = 2, max = 150, message = "Le terme doit comporter entre 2 et 150 caractères")
        @Pattern(regexp = "^[\\p{L}\\p{N}\\s,.'\\-]+$", message = "Le terme de recherche contient des caractères invalides")
        String query
) {
}
