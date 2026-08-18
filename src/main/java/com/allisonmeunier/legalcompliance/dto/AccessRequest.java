package com.allisonmeunier.legalcompliance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * category is a dotted key such as "document.view" or "case.seal" - the audit
 * service turns the dot into an underscore to build a Mongo-safe field name
 * (document.view -> history.document_view).
 */
public record AccessRequest(
        @NotBlank String category,
        @NotNull AccessChannel channel
) {
}
