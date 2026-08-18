package com.allisonmeunier.legalcompliance.compliance;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A case file can be subject to several retention rules at once (a data-protection
 * rule, a tax rule, a professional-conduct rule...), each covering only part of the
 * timeline. The document must satisfy all of them simultaneously, so the real
 * retention window is: the EARLIEST notBefore any rule states, until the LATEST
 * retainUntil any rule states.
 *
 * Example:
 *   Rule A: notBefore = 2024-01-10, retainUntil = 2027-01-20
 *   Rule B: notBefore = 2023-06-01, retainUntil = null
 *   Rule C: notBefore = null,       retainUntil = 2029-01-01
 *   -> combined period = [2023-06-01, 2029-01-01]
 *
 * If not a single rule specifies a notBefore (or not a single one specifies a
 * retainUntil), there's no bounded window to report - the result is empty rather
 * than a guess.
 */
@Component
public class RetentionPeriodCalculator {

    public Optional<RetentionPeriod> calculate(List<RetentionRule> rules) {
        Optional<LocalDate> earliestStart = rules.stream()
                .map(RetentionRule::notBefore)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);

        Optional<LocalDate> latestEnd = rules.stream()
                .map(RetentionRule::retainUntil)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo);

        if (earliestStart.isEmpty() || latestEnd.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new RetentionPeriod(earliestStart.get(), latestEnd.get()));
    }
}
