package com.allisonmeunier.legalcompliance.util;

public final class CaseReferenceMasker {

    private static final int MAX_VISIBLE_PREFIX_LENGTH = 3;

    private CaseReferenceMasker() {
    }

    public static String mask(String reference) {
        if (reference == null) {
            return null;
        }
        int visibleLength = Math.min(MAX_VISIBLE_PREFIX_LENGTH, reference.length() / 2);
        String visible = reference.substring(0, visibleLength);
        String hidden = "*".repeat(reference.length() - visibleLength);
        return visible + hidden;
    }
}
