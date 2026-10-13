package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
import com.allisonmeunier.legalcompliance.model.AccessLog;
import com.allisonmeunier.legalcompliance.repository.AccessLogRepository;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CaseAccessAuditService {

    private final CaseRepository caseRepository;
    private final AccessLogRepository accessLogRepository;

    public CaseAccessAuditService(CaseRepository caseRepository, AccessLogRepository accessLogRepository) {
        this.caseRepository = caseRepository;
        this.accessLogRepository = accessLogRepository;
    }

    public void recordAccess(String caseReference, String userId, String category, AccessChannel channel) {
        if (caseRepository.findByReference(caseReference).isEmpty()) {
            throw new CaseNotFoundException(caseReference);
        }
        accessLogRepository.save(new AccessLog(caseReference, userId, category, channel, Instant.now()));
    }
}
