package com.goodmaps.service;

import com.goodmaps.dto.ActivityDto;
import com.goodmaps.enums.AccessibilityProfile;
import com.goodmaps.enums.ActivityCategory;
import com.goodmaps.model.osm.OverpassElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ActivityMappingTest {

    private ActivityService activityService;
    private OverpassClient overpassClient;
    private RecommendationScoringService scoringService;

    @BeforeEach
    void setUp() {
        overpassClient = Mockito.mock(OverpassClient.class);
        scoringService = new RecommendationScoringService();
        activityService = new ActivityService(overpassClient, scoringService);
    }

    @Test
    @DisplayName("Le calcul de la distance Haversine doit être précis à quelques mètres près")
    void testHaversineDistance() {
        // Coordonnées Tour Eiffel (48.8584, 2.2945) et Arc de Triomphe (48.8738, 2.2950) -> environ 1715 mètres
        double distance = ActivityService.calculateHaversineDistanceMeters(48.8584, 2.2945, 48.8738, 2.2950);
        assertTrue(distance > 1650 && distance < 1780, "La distance attendue est d'environ 1715m, calculée: " + distance);
    }

    @Test
    @DisplayName("Le mapping d'un OverpassElement doit assainir les liens et classifier la catégorie correctement")
    void testElementMapping() {
        Map<String, String> tags = Map.of(
                "name", "Opéra Garnier",
                "tourism", "attraction",
                "wheelchair", "yes",
                "website", "http://operadeparis.fr",
                "phone", "+33 1 71 25 24 23",
                "addr:street", "Place de l'Opéra",
                "addr:housenumber", "8",
                "addr:city", "Paris"
        );

        OverpassElement element = new OverpassElement("node", 999888L, 48.8719, 2.3316, null, tags);
        ActivityDto dto = activityService.mapToActivityDto(element, 48.8700, 2.3300, AccessibilityProfile.PMR);

        assertNotNull(dto);
        assertEquals("Opéra Garnier", dto.name());
        assertEquals(ActivityCategory.CULTURE, dto.category());
        assertEquals("node/999888", dto.id());
        assertEquals("yes", dto.accessibility().wheelchair());
        // Vérification de la conversion sécurisée HTTP -> HTTPS
        assertEquals("https://operadeparis.fr", dto.contact().website());
        assertEquals("+33 1 71 25 24 23", dto.contact().phone());
        assertEquals("8 Place de l'Opéra, Paris", dto.contact().address());
        assertTrue(dto.osmUrl().contains("openstreetmap.org/node/999888"));
    }

    @Test
    @DisplayName("Un élément sans nom identifiable doit être ignoré")
    void testElementWithoutNameIgnored() {
        Map<String, String> tags = Map.of(
                "wheelchair", "yes",
                "amenity", "bench"
        );

        OverpassElement element = new OverpassElement("node", 111222L, 48.85, 2.34, null, tags);
        ActivityDto dto = activityService.mapToActivityDto(element, 48.85, 2.34, AccessibilityProfile.ALL);
        assertNull(dto, "Un lieu sans nom ne doit pas être mappé en activité");
    }
}
