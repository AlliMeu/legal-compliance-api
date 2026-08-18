package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaseAccessAuditServiceImplUnitTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private MongoTemplate mongoTemplate;

    private CaseAccessAuditServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CaseAccessAuditServiceImpl(caseRepository, mongoTemplate, true);
    }

    @Test
    void auditDisabled_noRepositoryLookupNoMongoCall() {
        CaseAccessAuditServiceImpl disabled =
                new CaseAccessAuditServiceImpl(caseRepository, mongoTemplate, false);

        disabled.recordAccess("REF-001", "user-1", "document.view");

        verifyNoInteractions(caseRepository);
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void caseNotFound_repositoryCalledButMongoIsNot() {
        when(caseRepository.findByReference("REF-404")).thenReturn(Optional.empty());

        service.recordAccess("REF-404", "user-1", "document.view");

        verify(caseRepository).findByReference("REF-404");
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void caseFound_upsertsOnceWithConvertedFieldName() {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));

        service.recordAccess("REF-001", "user-1", "document.view");

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq(CaseAccessAuditServiceImpl.COLLECTION));

        // Inspecting the $set document directly (rather than toJson()) because the plain
        // BSON codec registry used outside a real Mongo connection doesn't know how to
        // serialize java.time.Instant - Spring Data's own converters handle that at
        // runtime, but a raw Document.toJson() call in a unit test doesn't have them.
        org.bson.Document setFields = (org.bson.Document) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(setFields).containsKey("history.document_view.lastAccessedBy");
        assertThat(setFields.get("history.document_view.lastAccessedBy")).isEqualTo("user-1");
        assertThat(setFields.keySet()).noneMatch(key -> key.contains("document.view"));
    }

    @Test
    void duplicateKeyOnUpsert_recoversByUpdatingExistingRecord() {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));
        when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq(CaseAccessAuditServiceImpl.COLLECTION)))
                .thenThrow(new DuplicateKeyException("E11000 duplicate key"));

        service.recordAccess("REF-001", "user-1", "document.view");

        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(CaseAccessAuditServiceImpl.COLLECTION));
    }
}
