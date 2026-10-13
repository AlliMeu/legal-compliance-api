package com.allisonmeunier.legalcompliance.controller;

import com.allisonmeunier.legalcompliance.compliance.RetentionPeriodCalculator;
import com.allisonmeunier.legalcompliance.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComplianceController.class)
@Import({SecurityConfig.class, RetentionPeriodCalculator.class})
@WithMockUser(authorities = "SCOPE_case:read")
class ComplianceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postRules(String body) throws Exception {
        return mockMvc.perform(post("/compliance/retention-period")
                .contentType("application/json")
                .content(body));
    }

    @Test
    void boundedRules_returnTheCombinedWindow() throws Exception {
        postRules("""
                {"rules": [
                  {"ruleName": "tax", "notBefore": "2023-06-01", "retainUntil": null},
                  {"ruleName": "conduct", "notBefore": null, "retainUntil": "2029-01-01"}
                ]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notBefore").value("2023-06-01"))
                .andExpect(jsonPath("$.retainUntil").value("2029-01-01"));
    }

    @Test
    void rulesWithoutAnyStart_returnNoContent() throws Exception {
        postRules("""
                {"rules": [{"ruleName": "tax", "notBefore": null, "retainUntil": "2029-01-01"}]}""")
                .andExpect(status().isNoContent());
    }

    @Test
    void contradictoryRules_areRejectedWithTheFieldName() throws Exception {
        postRules("""
                {"rules": [
                  {"ruleName": "conduct", "notBefore": "2030-01-01", "retainUntil": null},
                  {"ruleName": "tax", "notBefore": null, "retainUntil": "2025-01-01"}
                ]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.location").value("rules"));
    }

    @Test
    void missingRuleList_isAValidationError() throws Exception {
        postRules("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").value("VALIDATION_ERROR"));
    }

    @Test
    void nullRuleInTheList_isAValidationError() throws Exception {
        postRules("{\"rules\": [null]}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").value("VALIDATION_ERROR"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void withoutReadScope_isForbidden() throws Exception {
        postRules("{\"rules\": []}")
                .andExpect(status().isForbidden());
    }
}
