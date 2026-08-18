package com.allisonmeunier.legalcompliance.util;

/**
 * Masks a case/client reference before it goes into logs.
 *
 * Case references are the kind of identifier that should never appear in full in
 * application logs - the same instinct that governs a privilege log or a client
 * matter list, just applied to a log line instead of a filing cabinet.
 *
 * "1234567890" -> "123456****"
 * "123456"     -> "123456"      (exactly the visible prefix length - no stars)
 * "123"        -> "123"         (shorter than the prefix - nothing to hide)
 * ""           -> ""
 * null         -> null          (documented, not an accident - see CaseReferenceMaskerUnitTest)
 */
public final class CaseReferenceMasker {

    private static final int VISIBLE_PREFIX_LENGTH = 6;

    private CaseReferenceMasker() {
    }

    public static String mask(String reference) {
        if (reference == null) {
            return null;
        }
        if (reference.length() <= VISIBLE_PREFIX_LENGTH) {
            return reference;
        }
        String visible = reference.substring(0, VISIBLE_PREFIX_LENGTH);
        String hidden = "*".repeat(reference.length() - VISIBLE_PREFIX_LENGTH);
        return visible + hidden;
    }
}
