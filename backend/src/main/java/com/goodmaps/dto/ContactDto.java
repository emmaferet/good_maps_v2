package com.goodmaps.dto;

/**
 * Coordonnées, contact et adresse vérifiée du lieu.
 */
public record ContactDto(
        String address,
        String phone,
        String website,
        String email
) {
    public static ContactDto empty() {
        return new ContactDto(null, null, null, null);
    }
}
