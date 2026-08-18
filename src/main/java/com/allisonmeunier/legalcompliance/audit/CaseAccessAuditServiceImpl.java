package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * upsertAccess(...)
 *      |
 *      +--> auditing disabled? ---- yes --> stop
 *      |
 *      +--> case found? ------ no --> stop
 *      |
 *      +--> build Mongo query/update
 *      |
 *      +--> upsert
 *              |
 *              +--> DuplicateKeyException?
 *                       |
 *                       +--> recover() -> update existing record
 */
@Service
public class CaseAccessAuditServiceImpl implements CaseAccessAuditService {

    static final String COLLECTION = "case_access_audit";

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
    public void recordAccess(String caseReference, String userId, String category) {
        if (!auditEnabled) {
            return;
        }
        if (caseRepository.findByReference(caseReference).isEmpty()) {
            return;
        }

        Query query = Query.query(Criteria.where("caseReference").is(caseReference));
        Update update = buildUpdate(caseReference, userId, category);

        try {
            mongoTemplate.upsert(query, update, COLLECTION);
        } catch (DuplicateKeyException ex) {
            recover(query, update);
        }
    }

    void recover(Query query, Update update) {
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }

    private Update buildUpdate(String caseReference, String userId, String category) {
        // "document.view" -> "history.document_view" - keeps the category readable
        // in the API while staying a valid, dot-free Mongo field path.
        String mongoField = "history." + category.replace('.', '_');
        return new Update()
                .set("caseReference", caseReference)
                .set(mongoField + ".lastAccessedBy", userId)
                .set(mongoField + ".lastAccessedAt", Instant.now());
    }
}
