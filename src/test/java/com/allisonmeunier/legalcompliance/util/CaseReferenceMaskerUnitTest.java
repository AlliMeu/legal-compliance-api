package com.allisonmeunier.legalcompliance.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseReferenceMaskerUnitTest {

    @Test
    void longReferenceKeepsSixVisibleCharsAndMasksTheRest() {
        assertThat(CaseReferenceMasker.mask("1234567890")).isEqualTo("123456****");
    }

    @Test
    void referenceExactlySixCharsLongIsNotMasked() {
        assertThat(CaseReferenceMasker.mask("123456")).isEqualTo("123456");
    }

    @Test
    void referenceShorterThanSixCharsIsNotMasked() {
        assertThat(CaseReferenceMasker.mask("123")).isEqualTo("123");
    }

    @Test
    void emptyStringReturnsEmptyString() {
        assertThat(CaseReferenceMasker.mask("")).isEqualTo("");
    }

    @Test
    void nullReturnsNull_documentedBehaviorNotAnAccident() {
        assertThat(CaseReferenceMasker.mask(null)).isNull();
    }
}
