package com.allisonmeunier.legalcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccessRequest(
        @NotBlank String category,
        @NotNull AccessChannel channel
) {
}
