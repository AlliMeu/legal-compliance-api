package com.allisonmeunier.legalcompliance.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerUnitTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badRequestExceptionWithFieldNameReturns400WithLocation() {
        BadRequestException ex = new BadRequestException("category must not be blank", "category");

        ResponseEntity<ApiError> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorId()).isEqualTo("BAD_REQUEST");
        assertThat(response.getBody().message()).isEqualTo("category must not be blank");
        assertThat(response.getBody().location()).isEqualTo("category");
    }

    @Test
    void caseNotFoundExceptionReturns404() {
        CaseNotFoundException ex = new CaseNotFoundException("REF-001");

        ResponseEntity<ApiError> response = handler.handleCaseNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().errorId()).isEqualTo("NOT_FOUND");
        assertThat(response.getBody().message()).contains("REF-001");
    }

    @Test
    void invalidEnumValueReturns400WithParsedFieldMessage() throws Exception {
        InvalidFormatException invalidFormat = InvalidFormatException.from(
                null, "bad enum value", "not-a-channel", AccessChannelStub.class);
        invalidFormat.prependPath(AccessRequestStub.class, "channel");
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("parse error", invalidFormat, null);

        ResponseEntity<ApiError> response = handler.handleFaultyRequestBody(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid value 'not-a-channel'");
        assertThat(response.getBody().location()).isEqualTo("channel");
    }

    @Test
    void unparseableBodyWithoutEnumCauseFallsBackToGenericMessage() {
        HttpMessageNotReadableException ex =
                new HttpMessageNotReadableException("totally broken json", (Throwable) null, null);

        ResponseEntity<ApiError> response = handler.handleFaultyRequestBody(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Error processing request body.");
    }

    @Test
    void methodArgumentNotValidBuildsOneReadableMessageFromFieldErrors() throws Exception {
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new AccessRequestStub(), "accessRequest");
        bindingResult.addError(new FieldError("accessRequest", "category", "must not be blank"));
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(
                GlobalExceptionHandlerUnitTest.class.getDeclaredMethod("dummyEndpoint", AccessRequestStub.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<ApiError> response = handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().errorId()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().message()).isEqualTo("category is invalid");
    }

    // referenced by reflection above purely to obtain a real, non-null MethodParameter -
    // MethodArgumentNotValidException's constructor needs one, it's never actually called.
    private void dummyEndpoint(AccessRequestStub stub) {
    }

    @Test
    void constraintViolationReturns400WithGenericMessage() {
        ConstraintViolationException ex = new ConstraintViolationException(Collections.emptySet());

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().message()).isEqualTo("Invalid request");
    }

    @Test
    void unexpectedExceptionReturns500() {
        ResponseEntity<ApiError> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().errorId()).isEqualTo("INTERNAL_ERROR");
    }

    // Minimal stand-ins so InvalidFormatException has a real target type/reference
    // path to report, without pulling the real DTOs (and their validation
    // annotations) into what should stay a pure, dependency-light unit test.
    private enum AccessChannelStub { PORTAL, EMAIL, INTERNAL }

    private static class AccessRequestStub {
        String category;
        AccessChannelStub channel;
    }
}
