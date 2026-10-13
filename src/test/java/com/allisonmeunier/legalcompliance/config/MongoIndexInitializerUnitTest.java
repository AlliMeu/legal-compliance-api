package com.allisonmeunier.legalcompliance.config;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoIndexInitializerUnitTest {

    @Mock
    private MongoTemplate mongoTemplate;
    @Mock
    private IndexOperations casesIndexOps;
    @Mock
    private IndexOperations auditIndexOps;

    @Test
    void createsAUniqueIndexOnEachReferenceField() {
        when(mongoTemplate.indexOps(MongoIndexInitializer.CASES_COLLECTION)).thenReturn(casesIndexOps);
        when(mongoTemplate.indexOps(MongoIndexInitializer.AUDIT_COLLECTION)).thenReturn(auditIndexOps);

        new MongoIndexInitializer(mongoTemplate).run(null);

        assertUniqueIndexOn(casesIndexOps, "reference");
        assertUniqueIndexOn(auditIndexOps, "caseReference");
    }

    private void assertUniqueIndexOn(IndexOperations indexOps, String field) {
        ArgumentCaptor<IndexDefinition> captor = ArgumentCaptor.forClass(IndexDefinition.class);
        verify(indexOps).ensureIndex(captor.capture());
        Document keys = captor.getValue().getIndexKeys();
        Document options = captor.getValue().getIndexOptions();
        assertThat(keys.keySet()).containsExactly(field);
        assertThat(options.getBoolean("unique")).isTrue();
    }
}
