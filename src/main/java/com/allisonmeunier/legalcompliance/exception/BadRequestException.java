package com.allisonmeunier.legalcompliance.exception;

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
