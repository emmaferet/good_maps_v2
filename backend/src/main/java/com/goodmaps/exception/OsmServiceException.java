package com.goodmaps.exception;

/**
 * Exception levée lors d'un échec de communication ou de traitement avec les APIs OpenStreetMap.
 */
public class OsmServiceException extends RuntimeException {
    public OsmServiceException(String message) {
        super(message);
    }

    public OsmServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
