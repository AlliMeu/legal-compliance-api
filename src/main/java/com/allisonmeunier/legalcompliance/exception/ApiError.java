package com.allisonmeunier.legalcompliance.exception;

public record ApiError(String errorId, String message, String location) {

    public static ApiError of(String errorId, String message) {
        return new ApiError(errorId, message, null);
    }

    public static ApiError of(String errorId, String message, String location) {
        return new ApiError(errorId, message, location);
    }
}
