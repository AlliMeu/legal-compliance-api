package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.compliance.RetentionPeriodCalculator;
import com.allisonmeunier.legalcompliance.dto.RetentionRequest;
import com.allisonmeunier.legalcompliance.dto.RetentionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/compliance")
public class ComplianceController {

    private final RetentionPeriodCalculator calculator;

    public ComplianceController(RetentionPeriodCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/retention-period")
    public ResponseEntity<RetentionResponse> retentionPeriod(@Valid @RequestBody RetentionRequest request) {
        return calculator.retainUntil(request.missionEndDate(), request.rules())
                .map(date -> ResponseEntity.ok(new RetentionResponse(date)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
