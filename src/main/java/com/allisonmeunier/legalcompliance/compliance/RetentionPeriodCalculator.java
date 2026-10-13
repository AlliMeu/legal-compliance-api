package com.allisonmeunier.legalcompliance.compliance;

import com.allisonmeunier.legalcompliance.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class RetentionPeriodCalculator {

    public Optional<RetentionPeriod> calculate(List<RetentionRule> rules) {
        if (rules == null) {
            return Optional.empty();
        }

        Optional<LocalDate> earliestStart = rules.stream()
                .filter(Objects::nonNull)
                .map(RetentionRule::notBefore)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo);

        Optional<LocalDate> latestEnd = rules.stream()
                .filter(Objects::nonNull)
                .map(RetentionRule::retainUntil)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo);

        if (earliestStart.isEmpty() || latestEnd.isEmpty()) {
            return Optional.empty();
        }
        if (earliestStart.get().isAfter(latestEnd.get())) {
            throw new BadRequestException(
                    "Retention rules conflict: earliest start " + earliestStart.get()
                            + " is after latest end " + latestEnd.get(),
                    "rules");
        }

        return Optional.of(new RetentionPeriod(earliestStart.get(), latestEnd.get()));
    }
}
