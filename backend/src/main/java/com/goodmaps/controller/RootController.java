package com.goodmaps.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur d'accueil fournissant la documentation interactive et l'annuaire des endpoints sur GET /.
 */
@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        return ResponseEntity.ok(Map.of(
                "application", "Good Maps API Backend",
                "version", "1.0.0",
                "status", "UP",
                "documentation", "Plateforme cartographique d'activités accessibles via OpenStreetMap",
                "endpoints", Map.of(
                        "status", "/api/activities/status",
                        "search", "/api/activities/search?latitude=48.8719&longitude=2.3316&radius=2000&profile=PMR",
                        "suggestions", "/api/activities/suggestions?latitude=48.8719&longitude=2.3316&profile=PMR",
                        "geocode", "/api/activities/geocode?query=Paris",
                        "health", "/actuator/health"
                )
        ));
    }
}
