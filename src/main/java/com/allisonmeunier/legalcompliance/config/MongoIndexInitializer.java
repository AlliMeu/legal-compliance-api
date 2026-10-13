package com.allisonmeunier.legalcompliance.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "compliance.mongo.ensure-indexes", havingValue = "true", matchIfMissing = true)
public class MongoIndexInitializer implements ApplicationRunner {

    public static final String CASES_COLLECTION = "cases";
    public static final String AUDIT_COLLECTION = "case_access_audit";

    private final MongoTemplate mongoTemplate;

    public MongoIndexInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        mongoTemplate.indexOps(CASES_COLLECTION)
                .ensureIndex(new Index().on("reference", Sort.Direction.ASC).unique());
        mongoTemplate.indexOps(AUDIT_COLLECTION)
                .ensureIndex(new Index().on("caseReference", Sort.Direction.ASC).unique());
    }
}
