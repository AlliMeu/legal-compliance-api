package com.allisonmeunier.legalcompliance.dto;

import com.allisonmeunier.legalcompliance.compliance.RetentionRule;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record RetentionRequest(LocalDate missionEndDate, @NotEmpty List<@NotNull RetentionRule> rules) {
}
