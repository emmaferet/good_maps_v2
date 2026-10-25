package com.goodmaps.controller;

import com.goodmaps.dto.ActivityDto;
import com.goodmaps.dto.GeocodeQueryDto;
import com.goodmaps.dto.GeocodeResultDto;
import com.goodmaps.dto.SearchQueryDto;
import com.goodmaps.service.ActivityService;
import com.goodmaps.service.NominatimClient;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST principal pour la recherche d'activités accessibles et le géocodage OpenStreetMap.
 */
@RestController
@RequestMapping("/api/activities")
public class AccessibilityActivityController {

    private static final Logger log = LoggerFactory.getLogger(AccessibilityActivityController.class);

    private final ActivityService activityService;
    private final NominatimClient nominatimClient;

    public AccessibilityActivityController(ActivityService activityService, NominatimClient nominatimClient) {
        this.activityService = activityService;
        this.nominatimClient = nominatimClient;
    }

    /**
     * Recherche d'activités accessibles autour de coordonnées GPS données.
     */
    @GetMapping("/search")
    public ResponseEntity<List<ActivityDto>> searchActivities(@Valid @ModelAttribute SearchQueryDto query) {
        log.info("Requête de recherche reçue : lat={}, lon={}, rayon={}m, profil={}, cat={}",
                query.latitude(), query.longitude(), query.radius(), query.profile(), query.category());

        List<ActivityDto> activities = activityService.searchActivities(query);
        return ResponseEntity.ok(activities);
    }

    /**
     * Récupération des meilleures suggestions d'activités (Bouton 'Obtenir des suggestions' de l'IHM).
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<ActivityDto>> getSuggestions(@Valid @ModelAttribute SearchQueryDto query) {
        log.info("Requête de suggestions intelligentes reçue : lat={}, lon={}, profil={}",
                query.latitude(), query.longitude(), query.profile());

        List<ActivityDto> suggestions = activityService.getTopSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    /**
     * Géocodage textuel d'une adresse ou commune via OpenStreetMap Nominatim.
     */
    @GetMapping("/geocode")
    public ResponseEntity<List<GeocodeResultDto>> geocode(@Valid @ModelAttribute GeocodeQueryDto query) {
        log.info("Requête de géocodage reçue pour : '{}'", query.query());

        List<GeocodeResultDto> results = nominatimClient.geocode(query.query());
        return ResponseEntity.ok(results);
    }

    /**
     * Endpoint d'état du service cartographique.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Good Maps REST Proxy",
                "osmSource", "OpenStreetMap & Overpass API",
                "version", "1.0.0"
        ));
    }
}
