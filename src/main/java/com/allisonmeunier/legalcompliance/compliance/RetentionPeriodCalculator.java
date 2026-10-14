package com.allisonmeunier.legalcompliance.compliance;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class RetentionPeriodCalculator {

    public Optional<LocalDate> retainUntil(LocalDate missionEndDate, List<RetentionRule> rules) {
        if (missionEndDate == null || rules == null || rules.isEmpty()) {
            return Optional.empty();
        }
        int longestPeriod = rules.stream()
                .mapToInt(RetentionRule::getYears)
                .max()
                .getAsInt();
        return Optional.of(missionEndDate.plusYears(longestPeriod));
    }
}
