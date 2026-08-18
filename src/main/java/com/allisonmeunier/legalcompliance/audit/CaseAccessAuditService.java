package com.allisonmeunier.legalcompliance.audit;

/**
 * Records who accessed a case file and when - the equivalent of a privilege log
 * or chain-of-custody record for a regulated document store.
 */
public interface CaseAccessAuditService {

    /**
     * @param caseReference the case being accessed
     * @param userId        who accessed it
     * @param category      a dotted access category, e.g. "document.view" or "case.seal"
     */
    void recordAccess(String caseReference, String userId, String category);
}
