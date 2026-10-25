package com.goodmaps.service;

import com.goodmaps.enums.AccessibilityProfile;
import com.goodmaps.model.osm.OverpassElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationScoringServiceTest {

    private RecommendationScoringService scoringService;

    @BeforeEach
    void setUp() {
        scoringService = new RecommendationScoringService();
    }

    @Test
    @DisplayName("Un lieu avec aménagements PMR complets doit obtenir un score élevé et être recommandé")
    void testPmrScoringSuccess() {
        Map<String, String> tags = Map.of(
                "name", "Musée d'Orsay",
                "wheelchair", "yes",
                "wheelchair:description", "Rampes d'accès et ascenseurs disponibles à chaque niveau",
                "toilets:wheelchair", "yes",
                "elevator", "yes",
                "opening_hours", "Tu-Su 09:30-18:00",
                "website", "https://www.musee-orsay.fr"
        );

        OverpassElement element = new OverpassElement("node", 123456L, 48.8599, 2.3265, null, tags);
        int score = scoringService.calculateScore(element, AccessibilityProfile.PMR, 300.0);

        assertTrue(score >= 70, "Le score calculé devrait être >= 70, obtenu: " + score);
        assertTrue(scoringService.isRecommended(score), "Le lieu devrait être labellisé 'Recommandé'");
    }

    @Test
    @DisplayName("Un lieu sans accès PMR (wheelchair=no) doit avoir un score pénalisé")
    void testPmrScoringInaccessible() {
        Map<String, String> tags = Map.of(
                "name", "Vieux bar avec escalier",
                "wheelchair", "no"
        );

        OverpassElement element = new OverpassElement("node", 654321L, 48.85, 2.34, null, tags);
        int score = scoringService.calculateScore(element, AccessibilityProfile.PMR, 800.0);

        assertTrue(score < 30, "Le score d'un lieu inaccessible PMR devrait être faible, obtenu: " + score);
        assertFalse(scoringService.isRecommended(score));
    }

    @Test
    @DisplayName("Un lieu avec bandes podotactiles et braille doit être recommandé pour le profil VISUAL")
    void testVisualScoring() {
        Map<String, String> tags = Map.of(
                "name", "Médiathèque des Aveugles",
                "tactile_paving", "yes",
                "braille", "yes",
                "audio_guidance", "yes",
                "opening_hours", "Mo-Fr 10:00-19:00"
        );

        OverpassElement element = new OverpassElement("node", 789101L, 48.86, 2.35, null, tags);
        int score = scoringService.calculateScore(element, AccessibilityProfile.VISUAL, 400.0);

        assertTrue(score >= 80, "Le score calculé pour VISUAL devrait être >= 80, obtenu: " + score);
        assertTrue(scoringService.isRecommended(score));
    }

    @Test
    @DisplayName("Un parc calme doit recevoir un bonus pour le profil COGNITIVE")
    void testCognitiveScoring() {
        Map<String, String> tags = Map.of(
                "name", "Parc des Buttes-Chaumont",
                "leisure", "park",
                "calm_environment", "yes"
        );

        OverpassElement element = new OverpassElement("way", 112233L, 48.88, 2.38, null, tags);
        int score = scoringService.calculateScore(element, AccessibilityProfile.COGNITIVE, 200.0);

        assertTrue(score >= 60, "Le score pour un parc calme doit être >= 60, obtenu: " + score);
        assertTrue(scoringService.isRecommended(score));
    }
}
