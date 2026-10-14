package com.allisonmeunier.legalcompliance.compliance;

/**
 * Belgian retention periods for a lawyer's case file, counted from the end of the mission.
 * The demo treats the end of the mission and the end of the business relationship as the same date.
 */
public enum RetentionRule {

    LAWYER_FILE(5),
    ANTI_MONEY_LAUNDERING(10);

    private final int years;

    RetentionRule(int years) {
        this.years = years;
    }

    public int getYears() {
        return years;
    }
}
