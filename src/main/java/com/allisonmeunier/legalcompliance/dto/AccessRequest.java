package com.allisonmeunier.legalcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AccessRequest(
        @NotBlank @Pattern(regexp = "[a-z]+(\\.[a-z]+)*") String category,
        @NotNull AccessChannel channel
) {
}
