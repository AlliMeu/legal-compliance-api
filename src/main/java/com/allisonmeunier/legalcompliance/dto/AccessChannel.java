package com.allisonmeunier.legalcompliance.dto;

/** How the access was made - part of AccessRequest, and the field that exercises
 * GlobalExceptionHandler's invalid-enum-in-JSON branch when the client sends a
 * value that isn't one of these three. */
public enum AccessChannel {
    PORTAL,
    EMAIL,
    INTERNAL
}
