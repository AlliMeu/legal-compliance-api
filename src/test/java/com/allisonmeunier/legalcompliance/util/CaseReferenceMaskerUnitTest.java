package com.allisonmeunier.legalcompliance.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CaseReferenceMaskerUnitTest {

    @Test
    void longReferenceKeepsThreeVisibleCharsAndMasksTheRest() {
        assertThat(CaseReferenceMasker.mask("1234567890")).isEqualTo("123*******");
    }

    @Test
    void referenceInTheApiFormatHidesItsDistinctivePart() {
        assertThat(CaseReferenceMasker.mask("REF-001")).isEqualTo("REF****");
    }

    @Test
    void sixCharReferenceShowsOnlyHalf() {
        assertThat(CaseReferenceMasker.mask("123456")).isEqualTo("123***");
    }

    @Test
    void shortReferenceNeverShowsMoreThanHalf() {
        assertThat(CaseReferenceMasker.mask("123")).isEqualTo("1**");
    }

    @Test
    void singleCharReferenceIsFullyMasked() {
        assertThat(CaseReferenceMasker.mask("1")).isEqualTo("*");
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
