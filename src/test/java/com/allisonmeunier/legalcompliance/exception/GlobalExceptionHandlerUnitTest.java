package com.allisonmeunier.legalcompliance.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void caseNotFoundExceptionReturns404() {
        CaseNotFoundException ex = new CaseNotFoundException("REF-001");

        ResponseEntity<ApiError> response = handler.handleCaseNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().errorId()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).contains("REF-001");
    }

    @Test
    void unreadableBodyReturns400() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("broken json", (Throwable) null, null);

        ResponseEntity<ApiError> response = handler.handleUnreadableBody(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid request body.");
    }
}
