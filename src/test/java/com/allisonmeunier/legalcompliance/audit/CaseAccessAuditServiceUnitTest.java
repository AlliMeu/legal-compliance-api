package com.allisonmeunier.legalcompliance.audit;

import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
import com.allisonmeunier.legalcompliance.model.AccessLog;
import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.AccessLogRepository;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAccessAuditServiceUnitTest {

    @Mock
    private CaseRepository caseRepository;
    @Mock
    private AccessLogRepository accessLogRepository;

    @InjectMocks
    private CaseAccessAuditService service;

    @Test
    void unknownCase_throwsAndNothingIsSaved() {
        when(caseRepository.findByReference("REF-404")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordAccess("REF-404", "attorney", "document.view", AccessChannel.PORTAL))
                .isInstanceOf(CaseNotFoundException.class);

        verifyNoInteractions(accessLogRepository);
    }

    @Test
    void knownCase_savesOneLogWithEveryField() {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));

        service.recordAccess("REF-001", "attorney", "document.view", AccessChannel.EMAIL);

        ArgumentCaptor<AccessLog> captor = ArgumentCaptor.forClass(AccessLog.class);
        verify(accessLogRepository).save(captor.capture());
        AccessLog saved = captor.getValue();
        assertThat(saved.getCaseReference()).isEqualTo("REF-001");
        assertThat(saved.getUserId()).isEqualTo("attorney");
        assertThat(saved.getCategory()).isEqualTo("document.view");
        assertThat(saved.getChannel()).isEqualTo(AccessChannel.EMAIL);
        assertThat(saved.getAccessedAt()).isNotNull();
    }
}
