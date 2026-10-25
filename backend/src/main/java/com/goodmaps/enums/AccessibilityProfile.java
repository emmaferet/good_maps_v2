package com.goodmaps.enums;

/**
 * Profils d'accessibilité pris en charge par Good Maps.
 */
public enum AccessibilityProfile {
    ALL("Tous profils confondus"),
    PMR("Personnes à Mobilité Réduite (Fauteuil roulant, rampe, ascenseur)"),
    VISUAL("Déficience visuelle (Bandes podotactiles, braille, guidage)"),
    AUDITORY("Déficience auditive (Boucle magnétique, sous-titrage)"),
    COGNITIVE("Déficience cognitive / mentale (Environnement calme, signalétique)");

    private final String description;

    AccessibilityProfile(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
