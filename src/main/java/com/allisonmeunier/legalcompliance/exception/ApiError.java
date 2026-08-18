package com.allisonmeunier.legalcompliance.exception;

/**
 * Uniform error body returned by every handler in GlobalExceptionHandler.
 * `location` is nullable - only set when the error can be traced to one specific field.
 */
public record ApiError(String errorId, String message, String location) {

    public static ApiError of(String errorId, String message) {
        return new ApiError(errorId, message, null);
    }

    public static ApiError of(String errorId, String message, String location) {
        return new ApiError(errorId, message, location);
    }
}
