package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.compliance.RetentionPeriod;
import com.allisonmeunier.legalcompliance.compliance.RetentionPeriodCalculator;
import com.allisonmeunier.legalcompliance.dto.RetentionRequest;
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
    public ResponseEntity<RetentionPeriod> retentionPeriod(@Valid @RequestBody RetentionRequest request) {
        return calculator.calculate(request.rules())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
