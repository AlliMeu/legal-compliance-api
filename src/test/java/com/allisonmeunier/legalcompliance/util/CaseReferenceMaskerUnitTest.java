package com.allisonmeunier.legalcompliance.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseReferenceMaskerUnitTest {

    @Test
    void longReferenceKeepsThreeVisibleCharsAndMasksTheRest() {
        assertThat(CaseReferenceMasker.mask("REF-001")).isEqualTo("REF****");
    }

    @Test
    void fourCharReferenceMasksOnlyTheLastChar() {
        assertThat(CaseReferenceMasker.mask("1234")).isEqualTo("123*");
    }

    @Test
    void referenceOfThreeCharsOrLessIsFullyMasked() {
        assertThat(CaseReferenceMasker.mask("123")).isEqualTo("***");
    }

    @Test
    void emptyStringReturnsEmptyString() {
        assertThat(CaseReferenceMasker.mask("")).isEqualTo("");
    }

    @Test
    void nullReturnsNull() {
        assertThat(CaseReferenceMasker.mask(null)).isNull();
    }
}
