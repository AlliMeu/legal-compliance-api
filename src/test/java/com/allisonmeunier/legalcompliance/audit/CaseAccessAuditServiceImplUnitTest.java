package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

        disabled.recordAccess("REF-001", "user-1", "document.view", AccessChannel.PORTAL);

        verifyNoInteractions(caseRepository);
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void caseNotFound_throwsAndNothingIsWritten() {
        when(caseRepository.findByReference("REF-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordAccess("REF-404", "user-1", "document.view", AccessChannel.PORTAL))
                .isInstanceOf(CaseNotFoundException.class);

        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void caseFound_upsertsOnceWithConvertedFieldNameAndChannel() {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));

        service.recordAccess("REF-001", "user-1", "document.view", AccessChannel.EMAIL);

        ArgumentCaptor<Update> updateCaptor = ArgumentCaptor.forClass(Update.class);
        verify(mongoTemplate).upsert(any(Query.class), updateCaptor.capture(), eq(CaseAccessAuditServiceImpl.COLLECTION));

        // Inspecting the $set document directly (rather than toJson()) because the plain
        // BSON codec registry used outside a real Mongo connection doesn't know how to
        // serialize java.time.Instant - Spring Data's own converters handle that at
        // runtime, but a raw Document.toJson() call in a unit test doesn't have them.
        org.bson.Document setFields = (org.bson.Document) updateCaptor.getValue().getUpdateObject().get("$set");
        assertThat(setFields.get("history.document_view.lastAccessedBy")).isEqualTo("user-1");
        assertThat(setFields.get("history.document_view.lastChannel")).isEqualTo("EMAIL");
        assertThat(setFields).containsKey("history.document_view.lastAccessedAt");
        assertThat(setFields.keySet()).noneMatch(key -> key.contains("document.view"));
    }

    @Test
    void duplicateKeyOnUpsert_recoversByUpdatingExistingRecord() {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));
        when(mongoTemplate.upsert(any(Query.class), any(Update.class), eq(CaseAccessAuditServiceImpl.COLLECTION)))
                .thenThrow(new DuplicateKeyException("E11000 duplicate key"));

        service.recordAccess("REF-001", "user-1", "document.view", AccessChannel.PORTAL);

        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(CaseAccessAuditServiceImpl.COLLECTION));
    }
}
