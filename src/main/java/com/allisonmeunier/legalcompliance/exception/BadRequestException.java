package com.allisonmeunier.legalcompliance.exception;

/**
 * Thrown for a request that is well-formed JSON but fails a business rule -
 * as opposed to validation annotation failures, which throw MethodArgumentNotValidException.
 */
public class BadRequestException extends RuntimeException {

    private final String fieldName;

    public BadRequestException(String message, String fieldName) {
        super(message);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
