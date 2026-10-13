package com.allisonmeunier.legalcompliance.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest()
                .body(ApiError.of("BAD_REQUEST", ex.getMessage(), ex.getFieldName()));
    }

    @ExceptionHandler(CaseNotFoundException.class)
    public ResponseEntity<ApiError> handleCaseNotFound(CaseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleFaultyRequestBody(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null
                && invalidFormat.getTargetType().isEnum()) {
            String path = invalidFormat.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "")
                    .filter(name -> !name.isEmpty())
                    .collect(Collectors.joining("."));
            String message = "Invalid value '" + invalidFormat.getValue() + "'";
            return ResponseEntity.badRequest().body(ApiError.of("BAD_REQUEST", message, path));
        }
        return ResponseEntity.badRequest()
                .body(ApiError.of("BAD_REQUEST", "Error processing request body."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getField)
                .map(field -> field + " is invalid")
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", message));
    }

    /**
     * The catch-all also receives Spring MVC's own exceptions (unsupported method,
     * unsupported media type, unknown path...). Those already carry the right status
     * through ErrorResponse, so they keep it instead of being turned into a 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            HttpStatusCode status = errorResponse.getStatusCode();
            String detail = errorResponse.getBody().getDetail();
            return ResponseEntity.status(status)
                    .headers(errorResponse.getHeaders())
                    .body(ApiError.of(errorIdFor(status), detail));
        }
        log.error("Unexpected error while handling a request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "Something went wrong."));
    }

    private static String errorIdFor(HttpStatusCode status) {
        HttpStatus known = HttpStatus.resolve(status.value());
        return known != null ? known.name() : "HTTP_" + status.value();
    }
}
