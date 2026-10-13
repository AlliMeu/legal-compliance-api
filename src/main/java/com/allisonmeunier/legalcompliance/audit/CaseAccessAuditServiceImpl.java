package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.config.MongoIndexInitializer;
import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CaseAccessAuditServiceImpl implements CaseAccessAuditService {

    static final String COLLECTION = MongoIndexInitializer.AUDIT_COLLECTION;

    private final CaseRepository caseRepository;
    private final MongoTemplate mongoTemplate;
    private final boolean auditEnabled;

    public CaseAccessAuditServiceImpl(CaseRepository caseRepository,
                                       MongoTemplate mongoTemplate,
                                       @Value("${compliance.audit.enabled:true}") boolean auditEnabled) {
        this.caseRepository = caseRepository;
        this.mongoTemplate = mongoTemplate;
        this.auditEnabled = auditEnabled;
    }

    @Override
    public void recordAccess(String caseReference, String userId, String category, AccessChannel channel) {
        if (!auditEnabled) {
            return;
        }
        if (caseRepository.findByReference(caseReference).isEmpty()) {
            throw new CaseNotFoundException(caseReference);
        }

        Query query = Query.query(Criteria.where("caseReference").is(caseReference));
        Update update = buildUpdate(caseReference, userId, category, channel);

        try {
            mongoTemplate.upsert(query, update, COLLECTION);
        } catch (DuplicateKeyException ex) {
            mongoTemplate.updateFirst(query, update, COLLECTION);
        }
    }

    private Update buildUpdate(String caseReference, String userId, String category, AccessChannel channel) {
        String mongoField = "history." + category.replace('.', '_');
        return new Update()
                .set("caseReference", caseReference)
                .set(mongoField + ".lastAccessedBy", userId)
                .set(mongoField + ".lastAccessedAt", Instant.now())
                .set(mongoField + ".lastChannel", channel.name());
    }
}
