package com.goodmaps.enums;

/**
 * Catégories d'activités et de lieux recensés dans Good Maps.
 */
public enum ActivityCategory {
    ALL("Toutes catégories"),
    CULTURE("Musée, monument, théâtre, galerie"),
    FOOD("Restaurant, café, bar"),
    LEISURE("Parc, cinéma, sport, attraction"),
    ACCOMMODATION("Hôtel, auberge, hébergement adapté"),
    SHOPPING("Commerces de proximité, centre commercial");

    private final String label;

    ActivityCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
