package com.allisonmeunier.legalcompliance.compliance;

import java.time.LocalDate;

public record RetentionPeriod(LocalDate notBefore, LocalDate retainUntil) {
}
