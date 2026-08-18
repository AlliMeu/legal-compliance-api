package com.allisonmeunier.legalcompliance.karate;

import com.allisonmeunier.legalcompliance.audit.CaseAccessAuditService;
import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import com.intuit.karate.junit5.Karate;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Optional;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Same API as CaseControllerSecurityIntegrationTest, exercised as a real black-box
 * HTTP client instead of through MockMvc - this is the BDD/API layer of the test
 * pyramid (Karate) sitting on top of the unit layer (JUnit/Mockito) below.
 *
 * The repository and audit service are mocked rather than backed by a real Mongo
 * instance, same reasoning as the MockMvc test: this suite is about proving the
 * HTTP contract (status codes, error shapes, auth rules), not about testing Mongo
 * itself.
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
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));
    }

    @Karate.Test
    Karate testCases() {
        return Karate.run("cases").relativeTo(getClass());
    }
}
