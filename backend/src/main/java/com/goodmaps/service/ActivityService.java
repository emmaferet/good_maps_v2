package com.goodmaps.service;

import com.goodmaps.dto.AccessibilityDetailsDto;
import com.goodmaps.dto.ActivityDto;
import com.goodmaps.dto.ContactDto;
import com.goodmaps.dto.SearchQueryDto;
import com.goodmaps.enums.AccessibilityProfile;
import com.goodmaps.enums.ActivityCategory;
import com.goodmaps.model.osm.OverpassElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service applicatif orchestrant la recherche, la qualification et la transformation des POIs OpenStreetMap.
 */
@Service
public class ActivityService {

    private static final Logger log = LoggerFactory.getLogger(ActivityService.class);

    private final OverpassClient overpassClient;
    private final RecommendationScoringService scoringService;

    public ActivityService(OverpassClient overpassClient, RecommendationScoringService scoringService) {
        this.overpassClient = overpassClient;
        this.scoringService = scoringService;
    }

    public List<ActivityDto> searchActivities(SearchQueryDto query) {
        List<OverpassElement> rawElements = overpassClient.fetchElements(
                query.latitude(),
                query.longitude(),
                query.radius(),
                query.category()
        );

        return rawElements.stream()
                .filter(el -> el.getEffectiveLat() != null && el.getEffectiveLon() != null)
                .filter(el -> hasValidNameOrType(el))
                .map(el -> mapToActivityDto(el, query.latitude(), query.longitude(), query.profile()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ActivityDto::recommended).reversed()
                        .thenComparing(Comparator.comparing(ActivityDto::score).reversed())
                        .thenComparing(ActivityDto::distanceMeters))
                .limit(query.limit())
                .toList();
    }

    public List<ActivityDto> getTopSuggestions(SearchQueryDto query) {
        List<ActivityDto> all = searchActivities(query);
        // Filtrer prioritairement les lieux avec badge "recommandé" ou les 5 meilleurs scores
        List<ActivityDto> recommended = all.stream()
                .filter(ActivityDto::recommended)
                .toList();

        if (!recommended.isEmpty()) {
            return recommended.stream().limit(10).toList();
        }
        return all.stream().limit(5).toList();
    }

    public ActivityDto mapToActivityDto(OverpassElement el, Double centerLat, Double centerLon, AccessibilityProfile profile) {
        Map<String, String> tags = el.tags();
        if (tags == null) tags = Map.of();

        String name = extractName(tags);
        if (name == null || name.isBlank()) {
            return null;
        }

        ActivityCategory category = determineCategory(tags);
        Double lat = el.getEffectiveLat();
        Double lon = el.getEffectiveLon();
        Double distance = calculateHaversineDistanceMeters(centerLat, centerLon, lat, lon);

        AccessibilityDetailsDto accessDto = extractAccessibility(tags);
        ContactDto contactDto = extractContact(tags);

        int score = scoringService.calculateScore(el, profile, distance);
        boolean isRecommended = scoringService.isRecommended(score);

        String id = (el.type() != null ? el.type() : "node") + "/" + el.id();
        String osmUrl = String.format("https://www.openstreetmap.org/%s/%d", el.type() != null ? el.type() : "node", el.id());

        String description = generateAccessibilitySummary(accessDto, name);
        String openingHours = tags.getOrDefault("opening_hours", null);
        Boolean openNow = computeOpenNow(openingHours);

        return new ActivityDto(
                id,
                name,
                category,
                lat,
                lon,
                Math.round(distance * 10.0) / 10.0,
                openingHours,
                openNow,
                description,
                accessDto,
                contactDto,
                score,
                isRecommended,
                osmUrl
        );
    }

    private boolean hasValidNameOrType(OverpassElement el) {
        if (el.tags() == null) return false;
        return el.tags().containsKey("name") || el.tags().containsKey("brand");
    }

    private String extractName(Map<String, String> tags) {
        if (tags.containsKey("name:fr")) return tags.get("name:fr");
        if (tags.containsKey("name")) return tags.get("name");
        if (tags.containsKey("brand")) return tags.get("brand");
        return null;
    }

    private ActivityCategory determineCategory(Map<String, String> tags) {
        if (tags.containsKey("tourism")) {
            String val = tags.get("tourism").toLowerCase();
            if (val.matches("hotel|motel|guest_house|hostel|camp_site")) return ActivityCategory.ACCOMMODATION;
            return ActivityCategory.CULTURE;
        }
        if (tags.containsKey("amenity")) {
            String val = tags.get("amenity").toLowerCase();
            if (val.matches("restaurant|cafe|bar|pub|fast_food|ice_cream")) return ActivityCategory.FOOD;
            if (val.matches("theatre|cinema|arts_centre|library")) return ActivityCategory.CULTURE;
        }
        if (tags.containsKey("leisure")) return ActivityCategory.LEISURE;
        if (tags.containsKey("shop")) return ActivityCategory.SHOPPING;
        if (tags.containsKey("historic")) return ActivityCategory.CULTURE;
        return ActivityCategory.CULTURE;
    }

    private AccessibilityDetailsDto extractAccessibility(Map<String, String> tags) {
        String wheelchair = tags.getOrDefault("wheelchair", "unknown");
        String wheelchairDesc = tags.get("wheelchair:description");
        Boolean toiletsWheelchair = "yes".equalsIgnoreCase(tags.get("toilets:wheelchair"));
        Boolean tactilePaving = "yes".equalsIgnoreCase(tags.get("tactile_paving"));
        Boolean hearingLoop = "yes".equalsIgnoreCase(tags.get("hearing_loop"));
        Boolean braille = "yes".equalsIgnoreCase(tags.get("braille"));
        String blindDesc = tags.get("blind:description");
        String deafDesc = tags.get("deaf:description");
        String levelAccess = tags.get("level");

        return new AccessibilityDetailsDto(
                wheelchair,
                wheelchairDesc,
                toiletsWheelchair,
                tactilePaving,
                hearingLoop,
                braille,
                blindDesc,
                deafDesc,
                levelAccess,
                tags
        );
    }

    private ContactDto extractContact(Map<String, String> tags) {
        String street = tags.getOrDefault("addr:street", "");
        String housenumber = tags.getOrDefault("addr:housenumber", "");
        String city = tags.getOrDefault("addr:city", "");
        String address = (housenumber + " " + street + (city.isBlank() ? "" : ", " + city)).trim();
        if (address.isBlank()) address = null;

        String phone = tags.getOrDefault("phone", tags.get("contact:phone"));
        String website = sanitizeHttpsUrl(tags.getOrDefault("website", tags.get("contact:website")));
        String email = tags.getOrDefault("email", tags.get("contact:email"));

        return new ContactDto(address, phone, website, email);
    }

    private String sanitizeHttpsUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) return null;
        String trimmed = rawUrl.trim();
        if (trimmed.startsWith("http://")) {
            trimmed = "https://" + trimmed.substring(7);
        } else if (!trimmed.startsWith("https://")) {
            trimmed = "https://" + trimmed;
        }
        try {
            URI uri = URI.create(trimmed);
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null) {
                return trimmed;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String generateAccessibilitySummary(AccessibilityDetailsDto acc, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("Découvrez ").append(name);
        if ("yes".equalsIgnoreCase(acc.wheelchair())) {
            sb.append(", entièrement aménagé pour les personnes à mobilité réduite (PMR).");
        } else if ("limited".equalsIgnoreCase(acc.wheelchair())) {
            sb.append(", partiellement accessible avec assistance pour les PMR.");
        } else {
            sb.append(". Contactez l'établissement pour valider les accès spécifiques.");
        }
        return sb.toString();
    }

    private Boolean computeOpenNow(String openingHours) {
        if (openingHours == null || openingHours.isBlank()) {
            return null; // Statut non renseigné dans OSM
        }
        if ("24/7".equalsIgnoreCase(openingHours.trim())) {
            return true;
        }
        // Pour les horaires standards, on indique actif par défaut s'il y a des horaires déclarés
        return true;
    }

    /**
     * Formule de Haversine pour le calcul de distance en mètres entre deux coordonnées GPS.
     */
    public static double calculateHaversineDistanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Rayon de la Terre en mètres
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
