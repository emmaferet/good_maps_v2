package com.goodmaps.service;

import com.goodmaps.enums.ActivityCategory;
import com.goodmaps.exception.OsmServiceException;
import com.goodmaps.model.osm.OverpassElement;
import com.goodmaps.model.osm.OverpassResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Locale;

/**
 * Service proxy pour l'API Overpass d'OpenStreetMap.
 * Exécute des requêtes Overpass QL ciblées sur un rayon géographique avec cache Caffeine (60 minutes).
 */
@Service
public class OverpassClient {

    private static final Logger log = LoggerFactory.getLogger(OverpassClient.class);

    private final WebClient webClient;
    private final String overpassUrl;

    public OverpassClient(
            WebClient osmWebClient,
            @Value("${goodmaps.osm.overpass-url:https://overpass-api.de/api/interpreter}") String overpassUrl
    ) {
        this.webClient = osmWebClient;
        this.overpassUrl = overpassUrl;
    }

    @Cacheable(value = "activitiesCache", key = "#lat + '_' + #lon + '_' + #radius + '_' + #category.name()")
    public List<OverpassElement> fetchElements(Double lat, Double lon, Integer radius, ActivityCategory category) {
        String query = buildOverpassQl(lat, lon, radius, category);
        log.info("Appel Overpass API pour lat={}, lon={}, rayon={}m, catégorie={} (Cache MISS)", lat, lon, radius, category);

        try {
            OverpassResponse response = webClient.post()
                    .uri(overpassUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("data", query))
                    .retrieve()
                    .bodyToMono(OverpassResponse.class)
                    .block();

            if (response == null || response.elements() == null) {
                log.warn("Réponse Overpass vide pour la zone [{}, {}]", lat, lon);
                return List.of();
            }

            log.info("Overpass a retourné {} éléments bruts.", response.elements().size());
            return response.elements();

        } catch (Exception ex) {
            log.error("Échec de la requête Overpass : {}", ex.getMessage());
            throw new OsmServiceException("Échec de la récupération des POIs Overpass: " + ex.getMessage(), ex);
        }
    }

    /**
     * Construit une requête Overpass QL optimisée et filtrée par rayon et tags d'activités.
     */
    public String buildOverpassQl(Double lat, Double lon, Integer radius, ActivityCategory category) {
        String filter = String.format(Locale.US, "(around:%d,%.6f,%.6f)", radius, lat, lon);
        StringBuilder sb = new StringBuilder();
        sb.append("[out:json][timeout:25];\n(");

        switch (category) {
            case CULTURE -> {
                sb.append(String.format("node[\"tourism\"~\"museum|gallery|artwork|attraction|monument\"]%s;\n", filter));
                sb.append(String.format("way[\"tourism\"~\"museum|gallery|artwork|attraction|monument\"]%s;\n", filter));
                sb.append(String.format("node[\"amenity\"~\"theatre|cinema|arts_centre|library\"]%s;\n", filter));
                sb.append(String.format("way[\"amenity\"~\"theatre|cinema|arts_centre|library\"]%s;\n", filter));
                sb.append(String.format("node[\"historic\"]%s;\n", filter));
                sb.append(String.format("way[\"historic\"]%s;\n", filter));
            }
            case FOOD -> {
                sb.append(String.format("node[\"amenity\"~\"restaurant|cafe|bar|pub|fast_food|ice_cream\"]%s;\n", filter));
                sb.append(String.format("way[\"amenity\"~\"restaurant|cafe|bar|pub|fast_food|ice_cream\"]%s;\n", filter));
            }
            case LEISURE -> {
                sb.append(String.format("node[\"leisure\"~\"park|garden|pitch|sports_centre|swimming_pool|water_park|nature_reserve\"]%s;\n", filter));
                sb.append(String.format("way[\"leisure\"~\"park|garden|pitch|sports_centre|swimming_pool|water_park|nature_reserve\"]%s;\n", filter));
                sb.append(String.format("node[\"tourism\"~\"theme_park|zoo|aquarium|viewpoint\"]%s;\n", filter));
                sb.append(String.format("way[\"tourism\"~\"theme_park|zoo|aquarium|viewpoint\"]%s;\n", filter));
            }
            case ACCOMMODATION -> {
                sb.append(String.format("node[\"tourism\"~\"hotel|motel|guest_house|hostel|camp_site\"]%s;\n", filter));
                sb.append(String.format("way[\"tourism\"~\"hotel|motel|guest_house|hostel|camp_site\"]%s;\n", filter));
            }
            case SHOPPING -> {
                sb.append(String.format("node[\"shop\"]%s;\n", filter));
                sb.append(String.format("way[\"shop\"]%s;\n", filter));
            }
            default -> { // ALL
                sb.append(String.format("node[\"tourism\"~\"museum|gallery|attraction|monument|hotel\"]%s;\n", filter));
                sb.append(String.format("way[\"tourism\"~\"museum|gallery|attraction|monument|hotel\"]%s;\n", filter));
                sb.append(String.format("node[\"amenity\"~\"theatre|cinema|arts_centre|restaurant|cafe|bar|library\"]%s;\n", filter));
                sb.append(String.format("way[\"amenity\"~\"theatre|cinema|arts_centre|restaurant|cafe|bar|library\"]%s;\n", filter));
                sb.append(String.format("node[\"leisure\"~\"park|garden|sports_centre\"]%s;\n", filter));
                sb.append(String.format("way[\"leisure\"~\"park|garden|sports_centre\"]%s;\n", filter));
                sb.append(String.format("node[\"historic\"]%s;\n", filter));
                sb.append(String.format("way[\"historic\"]%s;\n", filter));
            }
        }

        sb.append(");\nout center body qt 60;");
        return sb.toString();
    }
}
