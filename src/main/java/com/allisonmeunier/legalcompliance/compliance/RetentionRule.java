package com.allisonmeunier.legalcompliance.compliance;

import java.time.LocalDate;

public record RetentionRule(String ruleName, LocalDate notBefore, LocalDate retainUntil) {
}
