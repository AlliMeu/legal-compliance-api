package com.allisonmeunier.legalcompliance.karate;

import com.allisonmeunier.legalcompliance.audit.CaseAccessAuditService;
import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Optional;

import static org.mockito.Mockito.lenient;

/**
 * Real HTTP calls against a running Spring context. The repository and audit
 * service are mocked: MongoDB itself is checked by the docker-e2e-smoke CI job.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CaseApiKarateTest {

    @LocalServerPort
    private int port;

    @MockBean
    private CaseRepository caseRepository;

    @MockBean
    private CaseAccessAuditService auditService;

    @BeforeEach
    void setUp() {
        System.setProperty("karate.server.port", String.valueOf(port));
        lenient().when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Lease dispute", "OPEN")));
    }

    @Karate.Test
    Karate testCases() {
        return Karate.run("cases").relativeTo(getClass());
    }
}
