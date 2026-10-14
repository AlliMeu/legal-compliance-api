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

    private ResultActions postRetention(String body) throws Exception {
        return mockMvc.perform(post("/compliance/retention-period")
                .contentType("application/json")
                .content(body));
    }

    @Test
    void endedMission_returnsTheDateOfTheLongestRule() throws Exception {
        postRetention("""
                {"missionEndDate": "2026-03-15", "rules": ["LAWYER_FILE", "ANTI_MONEY_LAUNDERING"]}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.retainUntil").value("2036-03-15"));
    }

    @Test
    void missionNotEnded_returnsNoContent() throws Exception {
        postRetention("""
                {"missionEndDate": null, "rules": ["LAWYER_FILE"]}""")
                .andExpect(status().isNoContent());
    }

    @Test
    void noRule_isAValidationError() throws Exception {
        postRetention("""
                {"missionEndDate": "2026-03-15", "rules": []}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").value("VALIDATION_ERROR"));
    }

    @Test
    void unknownRule_isRejected() throws Exception {
        postRetention("""
                {"missionEndDate": "2026-03-15", "rules": ["TAX"]}""")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorId").value("BAD_REQUEST"));
    }

    @Test
    @WithMockUser(authorities = "SCOPE_case:write")
    void withoutReadScope_isForbidden() throws Exception {
        postRetention("""
                {"missionEndDate": "2026-03-15", "rules": ["LAWYER_FILE"]}""")
                .andExpect(status().isForbidden());
    }
}
