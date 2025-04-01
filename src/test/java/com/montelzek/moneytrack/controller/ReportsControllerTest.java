package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportsController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class ReportsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = {"USER", "PREMIUM"})
    public void showReports_premiumUser_shouldShowReportsPage() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("reports/reports"));
    }

    @Test
    @WithMockUser
    void showReports_userDoesNotHavePremium_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isForbidden());
    }
}
