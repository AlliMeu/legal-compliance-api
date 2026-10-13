package com.allisonmeunier.legalcompliance.util;

public final class CaseReferenceMasker {

    private static final int VISIBLE_PREFIX_LENGTH = 3;

    private CaseReferenceMasker() {
    }

    public static String mask(String reference) {
        if (reference == null) {
            return null;
        }
        if (reference.length() <= VISIBLE_PREFIX_LENGTH) {
            return "*".repeat(reference.length());
        }
        String visible = reference.substring(0, VISIBLE_PREFIX_LENGTH);
        String hidden = "*".repeat(reference.length() - VISIBLE_PREFIX_LENGTH);
        return visible + hidden;
    }
}
