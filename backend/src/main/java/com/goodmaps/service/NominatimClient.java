package com.goodmaps.service;

import com.goodmaps.dto.GeocodeResultDto;
import com.goodmaps.exception.OsmServiceException;
import com.goodmaps.model.osm.NominatimPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Service proxy pour l'API Nominatim d'OpenStreetMap.
 * Gère le géocodage textuel (ex: "Opéra Garnier", "Lyon", "Bordeaux") avec cache Caffeine anti-quota (24h).
 */
@Service
public class NominatimClient {

    private static final Logger log = LoggerFactory.getLogger(NominatimClient.class);

    private final WebClient webClient;
    private final String nominatimUrl;

    public NominatimClient(
            WebClient osmWebClient,
            @Value("${goodmaps.osm.nominatim-url:https://nominatim.openstreetmap.org}") String nominatimUrl
    ) {
        this.webClient = osmWebClient;
        this.nominatimUrl = nominatimUrl;
    }

    @Cacheable(value = "geocodingCache", key = "#query.trim().toLowerCase()")
    public List<GeocodeResultDto> geocode(String query) {
        log.info("Appel distant Nominatim pour la requête: '{}' (Cache MISS)", query);

        try {
            List<NominatimPlace> places = webClient.get()
                    .uri(nominatimUrl + "/search?q={query}&format=json&limit=5&addressdetails=1", query)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<NominatimPlace>>() {})
                    .block();

            if (places == null || places.isEmpty()) {
                log.info("Aucun résultat Nominatim pour: '{}'", query);
                return List.of();
            }

            return places.stream()
                    .map(this::mapToDto)
                    .toList();

        } catch (Exception ex) {
            log.error("Erreur lors de l'appel à Nominatim pour '{}' : {}", query, ex.getMessage());
            throw new OsmServiceException("Échec de la recherche géographique sur Nominatim: " + ex.getMessage(), ex);
        }
    }

    private GeocodeResultDto mapToDto(NominatimPlace place) {
        List<Double> bbox = new ArrayList<>();
        if (place.boundingbox() != null && place.boundingbox().size() == 4) {
            for (String val : place.boundingbox()) {
                try {
                    bbox.add(Double.parseDouble(val));
                } catch (NumberFormatException ignored) {}
            }
        }

        return new GeocodeResultDto(
                place.displayName(),
                place.getLatitude(),
                place.getLongitude(),
                place.type(),
                bbox
        );
    }
}
