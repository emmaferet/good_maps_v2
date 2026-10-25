package com.goodmaps.service;

import com.goodmaps.dto.AccessibilityDetailsDto;
import com.goodmaps.enums.AccessibilityProfile;
import com.goodmaps.model.osm.OverpassElement;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Service de scoring et de recommandation intelligente d'activités.
 * Calcule un score de pertinence (0 - 100) en pondérant :
 * 1. L'adéquation avec le profil de handicap choisi (PMR, visuel, auditif, cognitif).
 * 2. La complétude et la fiabilité des tags d'accessibilité dans OpenStreetMap.
 * 3. La présence d'informations pratiques vérifiées (horaires, contact, site internet).
 */
@Service
public class RecommendationScoringService {

    public int calculateScore(OverpassElement element, AccessibilityProfile profile, Double distanceMeters) {
        int score = 25; // Score de base pour tout lieu répertorié

        Map<String, String> tags = element.tags();
        if (tags == null || tags.isEmpty()) {
            return score;
        }

        // 1. Évaluation spécifique selon le profil sélectionné
        switch (profile) {
            case PMR -> score += evaluatePmrScore(tags);
            case VISUAL -> score += evaluateVisualScore(tags);
            case AUDITORY -> score += evaluateAuditoryScore(tags);
            case COGNITIVE -> score += evaluateCognitiveScore(tags);
            case ALL -> {
                // Pour ALL, on valorise la polyvalence des aménagements
                score += (evaluatePmrScore(tags) * 0.4);
                score += (evaluateVisualScore(tags) * 0.3);
                score += (evaluateAuditoryScore(tags) * 0.3);
            }
        }

        // 2. Bonus de complétude informationnelle (qualité SaaS)
        if (tags.containsKey("opening_hours")) score += 8;
        if (tags.containsKey("website") || tags.containsKey("contact:website")) score += 8;
        if (tags.containsKey("phone") || tags.containsKey("contact:phone")) score += 5;
        if (tags.containsKey("addr:street") || tags.containsKey("addr:housenumber")) score += 5;

        // 3. Modulateur de proximité
        if (distanceMeters != null) {
            if (distanceMeters < 500) {
                score += 10;
            } else if (distanceMeters < 1500) {
                score += 5;
            }
        }

        return Math.min(100, Math.max(0, score));
    }

    private int evaluatePmrScore(Map<String, String> tags) {
        int points = 0;
        String wheelchair = tags.getOrDefault("wheelchair", "").toLowerCase();
        switch (wheelchair) {
            case "yes" -> points += 35;
            case "limited" -> points += 18;
            case "no" -> points -= 25;
            default -> {}
        }

        if (tags.containsKey("wheelchair:description")) points += 12;
        if ("yes".equalsIgnoreCase(tags.get("toilets:wheelchair"))) points += 15;
        if ("yes".equalsIgnoreCase(tags.get("ramp"))) points += 10;
        if ("yes".equalsIgnoreCase(tags.get("elevator"))) points += 10;
        return points;
    }

    private int evaluateVisualScore(Map<String, String> tags) {
        int points = 0;
        if ("yes".equalsIgnoreCase(tags.get("tactile_paving"))) points += 30;
        if ("yes".equalsIgnoreCase(tags.get("braille"))) points += 25;
        if (tags.containsKey("blind:description")) points += 20;
        if ("yes".equalsIgnoreCase(tags.get("audio_guidance"))) points += 20;
        return points;
    }

    private int evaluateAuditoryScore(Map<String, String> tags) {
        int points = 0;
        if ("yes".equalsIgnoreCase(tags.get("hearing_loop"))) points += 35;
        if (tags.containsKey("deaf:description")) points += 25;
        if ("yes".equalsIgnoreCase(tags.get("subtitles"))) points += 20;
        return points;
    }

    private int evaluateCognitiveScore(Map<String, String> tags) {
        int points = 0;
        String leisure = tags.getOrDefault("leisure", "");
        String amenity = tags.getOrDefault("amenity", "");

        // Les parcs, bibliothèques et jardins sont particulièrement adaptés aux profils cognitifs (environnements apaisés)
        if ("park".equalsIgnoreCase(leisure) || "garden".equalsIgnoreCase(leisure) || "nature_reserve".equalsIgnoreCase(leisure)) {
            points += 30;
        } else if ("library".equalsIgnoreCase(amenity)) {
            points += 25;
        }

        if ("yes".equalsIgnoreCase(tags.get("calm_environment")) || "yes".equalsIgnoreCase(tags.get("quiet_room"))) {
            points += 30;
        }
        return points;
    }

    public boolean isRecommended(int score) {
        return score >= 60;
    }
}
