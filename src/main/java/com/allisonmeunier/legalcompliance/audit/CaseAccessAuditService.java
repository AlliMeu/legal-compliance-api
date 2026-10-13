package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.dto.AccessChannel;


public interface CaseAccessAuditService {

    void recordAccess(String caseReference, String userId, String category, AccessChannel channel);
}
