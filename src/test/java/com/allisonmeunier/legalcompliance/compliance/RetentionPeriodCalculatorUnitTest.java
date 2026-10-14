package com.allisonmeunier.legalcompliance.compliance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static com.allisonmeunier.legalcompliance.compliance.RetentionRule.ANTI_MONEY_LAUNDERING;
import static com.allisonmeunier.legalcompliance.compliance.RetentionRule.LAWYER_FILE;
import static org.assertj.core.api.Assertions.assertThat;

class RetentionPeriodCalculatorUnitTest {

    private final RetentionPeriodCalculator calculator = new RetentionPeriodCalculator();

    @Test
    void lawyerFileOnly_keptFiveYearsAfterTheEndOfTheMission() {
        assertThat(calculator.retainUntil(LocalDate.of(2026, 3, 15), List.of(LAWYER_FILE)))
                .contains(LocalDate.of(2031, 3, 15));
    }

    @Test
    void antiMoneyLaunderingOnly_keptTenYears() {
        assertThat(calculator.retainUntil(LocalDate.of(2026, 3, 15), List.of(ANTI_MONEY_LAUNDERING)))
                .contains(LocalDate.of(2036, 3, 15));
    }

    @Test
    void severalRules_theLongestPeriodWins() {
        assertThat(calculator.retainUntil(LocalDate.of(2026, 3, 15), List.of(LAWYER_FILE, ANTI_MONEY_LAUNDERING)))
                .contains(LocalDate.of(2036, 3, 15));
    }

    @Test
    void missionNotEnded_noDateIsReturned() {
        assertThat(calculator.retainUntil(null, List.of(LAWYER_FILE))).isEmpty();
    }

    @Test
    void noRule_noDateIsReturned() {
        assertThat(calculator.retainUntil(LocalDate.of(2026, 3, 15), List.of())).isEmpty();
        assertThat(calculator.retainUntil(LocalDate.of(2026, 3, 15), null)).isEmpty();
    }

    @Test
    void missionEndingOnFebruary29_landsOnFebruary28() {
        assertThat(calculator.retainUntil(LocalDate.of(2024, 2, 29), List.of(LAWYER_FILE)))
                .contains(LocalDate.of(2029, 2, 28));
    }
}
