package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.audit.CaseAccessAuditService;
import com.allisonmeunier.legalcompliance.config.SecurityConfig;
import com.allisonmeunier.legalcompliance.dto.AccessChannel;
import com.allisonmeunier.legalcompliance.exception.CaseNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CaseController.class)
@Import(SecurityConfig.class)
class CaseControllerSecurityIntegrationTest {

    private static final String VALID_ACCESS = "{\"category\":\"document.view\",\"channel\":\"PORTAL\"}";

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
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    void getCase_withWrongPassword_isUnauthorized() throws Exception {
        mockMvc.perform(get("/cases/REF-001").with(httpBasic("attorney", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postAccess_asAttorney_succeedsAndPassesTheChannel() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access").with(httpBasic("attorney", "demo"))
                        .contentType("application/json")
                        .content(VALID_ACCESS))
                .andExpect(status().isAccepted());

        verify(auditService).recordAccess("REF-001", "attorney", "document.view", AccessChannel.PORTAL);
    }

    @Test
    void postAccess_asParalegal_isForbidden() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access").with(httpBasic("paralegal", "demo"))
                        .contentType("application/json")
                        .content(VALID_ACCESS))
                .andExpect(status().isForbidden());
    }

    @Test
    void postAccess_withNoAuthentication_isUnauthorized() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access")
                        .contentType("application/json")
                        .content(VALID_ACCESS))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void postAccess_onUnknownCase_isNotFound() throws Exception {
        doThrow(new CaseNotFoundException("REF-404"))
                .when(auditService).recordAccess(anyString(), anyString(), anyString(), any());

        mockMvc.perform(post("/cases/REF-404/access")
                        .contentType("application/json")
                        .content(VALID_ACCESS))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorId").value("NOT_FOUND"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void postAccess_withNonJsonBody_isUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/cases/REF-001/access")
                        .contentType("text/plain")
                        .content("x"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(authorities = {"SCOPE_case:read", "SCOPE_case:write"})
    void unsupportedMethod_isMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/cases/REF-001"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:read")
    void unknownPath_isNotFound() throws Exception {
        mockMvc.perform(get("/cases"))
                .andExpect(status().isNotFound());
    }
}
