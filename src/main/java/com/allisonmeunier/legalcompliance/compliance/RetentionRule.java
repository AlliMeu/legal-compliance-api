package com.allisonmeunier.legalcompliance.compliance;

import java.time.LocalDate;

/**
 * One regulation's retention requirement for a case file. Either bound may be
 * absent - a rule might only say "keep from X" or only "keep until Y".
 */
public record RetentionRule(String ruleName, LocalDate notBefore, LocalDate retainUntil) {
}
