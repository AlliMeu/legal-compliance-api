package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.audit.CaseAccessAuditService;
import com.allisonmeunier.legalcompliance.config.SecurityConfig;
import com.allisonmeunier.legalcompliance.model.Case;
import com.allisonmeunier.legalcompliance.repository.CaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET  /cases/...          -> needs SCOPE_case:read
 * POST /cases/.../access    -> needs SCOPE_case:write
 *
 * Wrong scope  -> 403 Forbidden
 * No auth      -> 401 Unauthorized
 * Correct auth -> normal response
 */
@WebMvcTest(CaseController.class)
@Import(SecurityConfig.class)
class CaseControllerSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CaseRepository caseRepository;

    @MockBean
    private CaseAccessAuditService auditService;

    @Test
    @WithMockUser(authorities = "SCOPE_case:read")
    void getCase_withReadScope_succeeds() throws Exception {
        when(caseRepository.findByReference("REF-001"))
                .thenReturn(Optional.of(new Case("REF-001", "Matter A", "OPEN")));

        mockMvc.perform(get("/cases/REF-001"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void getCase_withOnlyWriteScope_isForbidden() throws Exception {
        mockMvc.perform(get("/cases/REF-001"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCase_withNoAuthentication_isUnauthorized() throws Exception {
        mockMvc.perform(get("/cases/REF-001"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void postAccess_withWriteScope_succeeds() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access")
                        .contentType("application/json")
                        .content("{\"category\":\"document.view\",\"channel\":\"PORTAL\"}"))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:read")
    void postAccess_withOnlyReadScope_isForbidden() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access")
                        .contentType("application/json")
                        .content("{\"category\":\"document.view\",\"channel\":\"PORTAL\"}"))
                .andExpect(status().isForbidden());
    }
}
