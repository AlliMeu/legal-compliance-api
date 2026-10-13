package com.allisonmeunier.legalcompliance.dto;

import com.allisonmeunier.legalcompliance.compliance.RetentionRule;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record RetentionRequest(@NotNull List<@NotNull RetentionRule> rules) {
}
