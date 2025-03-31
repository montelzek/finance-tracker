package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.BudgetService;
import com.montelzek.moneytrack.service.CategoryService;
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
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BudgetController.class)
@ExtendWith(MockitoExtension.class)
public class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CategoryService categoryService;

    private User testUser;
    private Budget budget;
    private BudgetDTO budgetDTO;
    private Category category;

    @BeforeEach
    public void setup() {
        testUser = User.builder().id(1L).build();

        category = Category.builder()
                .id(1)
                .type("EXPENSE")
                .build();

        budget = Budget.builder()
                .id(1L)
                .name("Test Budget")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .budgetSize(BigDecimal.valueOf(3000))
                .budgetSpent(BigDecimal.ZERO)
                .user(testUser)
                .category(category)
                .build();

        budgetDTO = BudgetDTO.builder()
                .id(1L)
                .name("Test Budget")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(14))
                .budgetSize(BigDecimal.valueOf(3000))
                .categoryId(category.getId())
                .build();
    }

    @Test
    @WithMockUser
    public void listBudget_shouldReturnListViewWithBudgets() throws Exception {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(budgetService.findUsersBudgets(testUser.getId()))
                .thenReturn(List.of(budget));
        when(categoryService.findByType(category.getType())).thenReturn(List.of(category));

        // Act & Assert
        mockMvc.perform(get("/budgets"))
                .andExpect(status().isOk())
                .andExpect(view().name("budgets/list"))
                .andExpect(model().attributeExists("budget", "budgets", "expenseCategories"));

        verify(userService).getCurrentUserId();
        verify(budgetService).findUsersBudgets(testUser.getId());
        verify(categoryService).findByType(category.getType());
    }

    @Test
    @WithMockUser
    public void getBudgetForEdit_shouldReturnAccountDTOasJson() throws Exception {
        // Arrange
        when(budgetService.findById(1L)).thenReturn(budget);
        when(budgetService.convertToDTO(budget)).thenReturn(budgetDTO);

        // Act & Assert
        mockMvc.perform(get("/budgets/edit/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Budget"));

        verify(budgetService).findById(1L);
        verify(budgetService).convertToDTO(budget);
    }

    @Test
    @WithMockUser
    public void saveBudget_successfulSave_shouldRedirectToBudget() throws Exception {
        // Arrange
        doNothing().when(budgetService).saveBudget(budgetDTO);

        // Act & Assert
        mockMvc.perform(post("/budgets/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("budget", budgetDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/budgets"));

        verify(budgetService).saveBudget(budgetDTO);
    }

    @Test
    @WithMockUser
    public void saveBudget_invalidData_shouldReturnBudgetListView() throws Exception {
        // Arrange
        budgetDTO.setBudgetSize(null);

        // Act & Assert
        mockMvc.perform(post("/budgets/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("budget", budgetDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("budgets/list"))
                .andExpect(model().attributeHasFieldErrors("budget", "budgetSize"));

        verify(budgetService, never()).saveBudget(budgetDTO);
    }

    @Test
    @WithMockUser
    public void saveBudget_illegalArgumentException_shouldReturnListWithViewWithError() throws Exception{
        // Arrange
        doThrow(new IllegalArgumentException()).when(budgetService).saveBudget(budgetDTO);

        // Act & Arrange
        mockMvc.perform(post("/budgets/save")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("budget", budgetDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("budgets/list"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("budget"));

        verify(budgetService).saveBudget(budgetDTO);
    }
}
