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
    private final List<String> mirrorUrls;

    public OverpassClient(
            WebClient osmWebClient,
            @Value("${goodmaps.osm.overpass-url:https://overpass.openstreetmap.fr/api/interpreter}") String overpassUrl
    ) {
        this.webClient = osmWebClient;
        // Liste ordonnée de miroirs avec priorité à l'instance configurée
        java.util.LinkedHashSet<String> mirrors = new java.util.LinkedHashSet<>();
        if (overpassUrl != null && !overpassUrl.isBlank()) {
            mirrors.add(overpassUrl.trim());
        }
        mirrors.add("https://overpass.openstreetmap.fr/api/interpreter");
        mirrors.add("https://overpass.kumi.systems/api/interpreter");
        mirrors.add("https://overpass-api.de/api/interpreter");
        this.mirrorUrls = new java.util.ArrayList<>(mirrors);
    }

    @Cacheable(value = "activitiesCache", key = "#lat + '_' + #lon + '_' + #radius + '_' + #category.name()")
    public List<OverpassElement> fetchElements(Double lat, Double lon, Integer radius, ActivityCategory category) {
        String query = buildOverpassQl(lat, lon, radius, category);
        log.info("Appel Overpass API pour lat={}, lon={}, rayon={}m, catégorie={} (Cache MISS)", lat, lon, radius, category);

        Exception lastException = null;

        for (String url : mirrorUrls) {
            try {
                log.info("Tentative de requête sur le serveur Overpass : {}", url);
                OverpassResponse response = webClient.post()
                        .uri(url)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(BodyInserters.fromFormData("data", query))
                        .retrieve()
                        .bodyToMono(OverpassResponse.class)
                        .block();

                if (response != null && response.elements() != null) {
                    log.info("Succès Overpass [{}] : {} éléments bruts reçus.", url, response.elements().size());
                    return response.elements();
                }
            } catch (Exception ex) {
                log.warn("Miroir Overpass [{}] indisponible ou en erreur : {}. Basculement automatique...", url, ex.getMessage());
                lastException = ex;
            }
        }

        log.error("Tous les miroirs Overpass ont échoué pour [{}, {}].", lat, lon);
        throw new OsmServiceException("Échec de la récupération des POIs Overpass sur tous les miroirs: " 
                + (lastException != null ? lastException.getMessage() : "Aucune réponse valide"), lastException);
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

        sb.append(");\nout body center qt 60;");
        return sb.toString();
    }
}
