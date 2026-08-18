package com.allisonmeunier.legalcompliance.compliance;

import java.time.LocalDate;

/** The combined window that satisfies every applicable RetentionRule at once. */
public record RetentionPeriod(LocalDate notBefore, LocalDate retainUntil) {
}
