package com.goodmaps.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire d'exceptions global produisant des réponses standardisées conformes au standard RFC 7807 (ProblemDetail).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Erreur de validation des paramètres : {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Les paramètres fournis dans la requête ne respectent pas les contraintes de validation."
        );
        problem.setTitle("Erreur de validation");
        problem.setType(URI.create("https://goodmaps.app/errors/validation"));
        problem.setProperty("timestamp", Instant.now());

        Map<String, String> invalidFields = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            invalidFields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        problem.setProperty("invalidParams", invalidFields);

        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argument invalide : {}", ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Paramètre non valide");
        problem.setType(URI.create("https://goodmaps.app/errors/bad-argument"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }

    @ExceptionHandler(OsmServiceException.class)
    public ProblemDetail handleOsmServiceException(OsmServiceException ex) {
        log.error("Échec du service proxy OpenStreetMap : {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY,
                "Le service externe OpenStreetMap (Overpass/Nominatim) est temporairement indisponible ou a retourné une erreur."
        );
        problem.setTitle("Erreur de service cartographique externe");
        problem.setType(URI.create("https://goodmaps.app/errors/osm-gateway-error"));
        problem.setProperty("timestamp", Instant.now());
        problem.setProperty("rootCause", ex.getMessage());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        log.error("Une erreur inattendue est survenue : {}", ex.getMessage(), ex);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne inattendue s'est produite. Veuillez réessayer ultérieurement."
        );
        problem.setTitle("Erreur interne du serveur");
        problem.setType(URI.create("https://goodmaps.app/errors/internal-error"));
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
