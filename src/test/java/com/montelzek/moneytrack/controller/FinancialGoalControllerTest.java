package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.FinancialGoalService;
import com.montelzek.moneytrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FinancialGoalController.class)
@ExtendWith(MockitoExtension.class)
public class FinancialGoalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FinancialGoalService financialGoalService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private FinancialGoal financialGoal;
    private FinancialGoalDTO financialGoalDTO;

    @BeforeEach
    public void setup() {
        testUser = User.builder().id(1L).build();

        financialGoal = FinancialGoal.builder()
                .id(1L)
                .name("Test Financial Goal")
                .targetAmount(BigDecimal.valueOf(2000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(testUser)
                .build();

        financialGoalDTO = FinancialGoalDTO.builder()
                .id(1L)
                .name("Test Financial Goal")
                .targetAmount(BigDecimal.valueOf(2000))
                .build();
    }

    @Test
    @WithMockUser
    public void listFinancialGoal_shouldReturnListViewWithFinancialGoals() throws Exception {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(financialGoalService.findUsersFinancialGoals(testUser.getId()))
                .thenReturn(List.of(financialGoal));

        // Act & Assert
        mockMvc.perform(get("/financialGoals"))
                .andExpect(status().isOk())
                .andExpect(view().name("financialGoals/list"))
                .andExpect(model().attributeExists("financialGoal", "financialGoals"));

        verify(userService).getCurrentUserId();
        verify(financialGoalService).findUsersFinancialGoals(testUser.getId());
    }

    @Test
    @WithMockUser
    public void getFinancialGoalForEdit_shouldReturnFinancialGoalDTOasJson() throws Exception {
        // Arrange
        when(financialGoalService.findById(1L)).thenReturn(financialGoal);
        when(financialGoalService.convertToDTO(financialGoal)).thenReturn(financialGoalDTO);

        // Act & Assert
        mockMvc.perform(get("/financialGoals/edit/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Financial Goal"));

        verify(financialGoalService).findById(1L);
        verify(financialGoalService).convertToDTO(financialGoal);
    }

    @Test
    @WithMockUser
    public void saveFinancialGoal_successfulSave_shouldRedirectToFinancialGoal() throws Exception {
        // Arrange
        doNothing().when(financialGoalService).saveFinancialGoal(financialGoalDTO);

        // Act & Assert
        mockMvc.perform(post("/financialGoals/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("financialGoal", financialGoalDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/financialGoals"));

        verify(financialGoalService).saveFinancialGoal(financialGoalDTO);
    }

    @Test
    @WithMockUser
    public void saveFinancialGoal_invalidData_shouldReturnFinancialGoalListView() throws Exception {
        // Arrange
        financialGoalDTO.setTargetAmount(null);

        // Act & Assert
        mockMvc.perform(post("/financialGoals/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("financialGoal", financialGoalDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("financialGoals/list"))
                .andExpect(model().attributeHasFieldErrors("financialGoal", "targetAmount"));

        verify(financialGoalService, never()).saveFinancialGoal(financialGoalDTO);
    }

    @Test
    @WithMockUser
    public void saveFinancialGoal_illegalArgumentException_shouldReturnListWithViewWithError() throws Exception{
        // Arrange
        doThrow(new IllegalArgumentException()).when(financialGoalService).saveFinancialGoal(financialGoalDTO);

        // Act & Arrange
        mockMvc.perform(post("/financialGoals/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("financialGoal", financialGoalDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("financialGoals/list"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("financialGoal"));

        verify(financialGoalService).saveFinancialGoal(financialGoalDTO);
    }
}
