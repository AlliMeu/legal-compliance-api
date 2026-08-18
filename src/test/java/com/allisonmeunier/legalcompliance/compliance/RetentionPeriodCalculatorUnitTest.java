package com.allisonmeunier.legalcompliance.compliance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RetentionPeriodCalculatorUnitTest {

    private final RetentionPeriodCalculator calculator = new RetentionPeriodCalculator();

    @Test
    void allRulesHaveBothBounds_returnsEarliestStartAndLatestEnd() {
        List<RetentionRule> rules = List.of(
                new RetentionRule("data-protection", LocalDate.of(2024, 1, 10), LocalDate.of(2027, 1, 20)),
                new RetentionRule("tax", LocalDate.of(2023, 6, 1), LocalDate.of(2026, 1, 1)),
                new RetentionRule("conduct", LocalDate.of(2024, 3, 1), LocalDate.of(2029, 1, 1))
        );

        Optional<RetentionPeriod> result = calculator.calculate(rules);

        assertThat(result).isPresent();
        assertThat(result.get().notBefore()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(result.get().retainUntil()).isEqualTo(LocalDate.of(2029, 1, 1));
    }

    @Test
    void allNotBeforeValuesNull_returnsEmpty() {
        List<RetentionRule> rules = List.of(
                new RetentionRule("tax", null, LocalDate.of(2026, 1, 1)),
                new RetentionRule("conduct", null, LocalDate.of(2029, 1, 1))
        );

        assertThat(calculator.calculate(rules)).isEmpty();
    }

    @Test
    void allRetainUntilValuesNull_returnsEmpty() {
        List<RetentionRule> rules = List.of(
                new RetentionRule("tax", LocalDate.of(2023, 6, 1), null),
                new RetentionRule("conduct", LocalDate.of(2024, 3, 1), null)
        );

        assertThat(calculator.calculate(rules)).isEmpty();
    }

    @Test
    void mixedMissingValues_stillFindsCorrectMinAndMaxAcrossRules() {
        List<RetentionRule> rules = List.of(
                new RetentionRule("data-protection", LocalDate.of(2024, 1, 10), LocalDate.of(2027, 1, 20)),
                new RetentionRule("tax", LocalDate.of(2023, 6, 1), null),
                new RetentionRule("conduct", null, LocalDate.of(2029, 1, 1))
        );

        Optional<RetentionPeriod> result = calculator.calculate(rules);

        assertThat(result).contains(new RetentionPeriod(LocalDate.of(2023, 6, 1), LocalDate.of(2029, 1, 1)));
    }

    @Test
    void emptyRuleList_returnsEmpty() {
        assertThat(calculator.calculate(List.of())).isEmpty();
    }

    @Test
    void singleRuleWithBothBounds_returnsThatRulesWindow() {
        List<RetentionRule> rules = List.of(
                new RetentionRule("only-rule", LocalDate.of(2025, 1, 1), LocalDate.of(2030, 1, 1))
        );

        assertThat(calculator.calculate(rules))
                .contains(new RetentionPeriod(LocalDate.of(2025, 1, 1), LocalDate.of(2030, 1, 1)));
    }
}
