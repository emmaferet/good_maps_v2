package com.goodmaps.controller;

import com.goodmaps.dto.ActivityDto;
import com.goodmaps.dto.GeocodeResultDto;
import com.goodmaps.dto.SearchQueryDto;
import com.goodmaps.enums.ActivityCategory;
import com.goodmaps.service.ActivityService;
import com.goodmaps.service.NominatimClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccessibilityActivityController.class)
class AccessibilityActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private NominatimClient nominatimClient;

    @Test
    @DisplayName("GET /api/activities/status doit retourner un statut UP 200 OK")
    void testStatusEndpoint() throws Exception {
        mockMvc.perform(get("/api/activities/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Good Maps REST Proxy"));
    }

    @Test
    @DisplayName("GET /api/activities/search sans latitude/longitude doit retourner une erreur 400 ProblemDetail")
    void testSearchMissingCoordinatesFails() throws Exception {
        mockMvc.perform(get("/api/activities/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Erreur de validation"))
                .andExpect(jsonPath("$.invalidParams.latitude").exists())
                .andExpect(jsonPath("$.invalidParams.longitude").exists());
    }

    @Test
    @DisplayName("GET /api/activities/search avec coordonnées hors limites (> 90.0) doit retourner 400")
    void testSearchOutOfBoundsCoordinates() throws Exception {
        mockMvc.perform(get("/api/activities/search")
                        .param("latitude", "95.5")
                        .param("longitude", "2.35"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams.latitude").value("La latitude maximale est 90.0"));
    }

    @Test
    @DisplayName("GET /api/activities/search avec paramètres valides doit retourner 200 OK et les résultats")
    void testSearchValidCoordinatesSuccess() throws Exception {
        ActivityDto mockDto = new ActivityDto(
                "node/1", "Opéra Garnier", ActivityCategory.CULTURE,
                48.8719, 2.3316, 250.0, "10:00-18:00", true,
                "Lieu accessible PMR", null, null, 85, true, "https://osm.org/node/1"
        );

        when(activityService.searchActivities(any(SearchQueryDto.class)))
                .thenReturn(List.of(mockDto));

        mockMvc.perform(get("/api/activities/search")
                        .param("latitude", "48.8719")
                        .param("longitude", "2.3316")
                        .param("radius", "2000")
                        .param("profile", "PMR")
                        .param("category", "CULTURE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Opéra Garnier"))
                .andExpect(jsonPath("$[0].category").value("CULTURE"))
                .andExpect(jsonPath("$[0].recommended").value(true));
    }

    @Test
    @DisplayName("GET /api/activities/geocode avec requête valide doit retourner 200 OK")
    void testGeocodeSuccess() throws Exception {
        when(nominatimClient.geocode("Paris"))
                .thenReturn(List.of(new GeocodeResultDto("Paris, France", 48.8566, 2.3522, "city", List.of())));

        mockMvc.perform(get("/api/activities/geocode")
                        .param("query", "Paris"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Paris, France"))
                .andExpect(jsonPath("$[0].latitude").value(48.8566));
    }

    @Test
    @DisplayName("GET /api/activities/geocode avec chaîne trop courte (< 2 caractères) doit retourner 400")
    void testGeocodeTooShortFails() throws Exception {
        mockMvc.perform(get("/api/activities/geocode")
                        .param("query", "A"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalidParams.query").exists());
    }
}
