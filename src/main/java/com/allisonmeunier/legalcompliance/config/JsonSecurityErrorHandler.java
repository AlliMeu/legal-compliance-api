package com.allisonmeunier.legalcompliance.config;

import com.allisonmeunier.legalcompliance.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JsonSecurityErrorHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"legal-compliance-api\"");
        write(response, HttpStatus.UNAUTHORIZED,
                ApiError.of("UNAUTHORIZED", "Authentication is required to access this resource."));
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(response, HttpStatus.FORBIDDEN,
                ApiError.of("FORBIDDEN", "You do not have the permission required for this action."));
    }

    private void write(HttpServletResponse response, HttpStatus status, ApiError body) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
