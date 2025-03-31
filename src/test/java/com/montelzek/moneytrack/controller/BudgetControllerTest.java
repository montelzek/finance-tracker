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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                .build();
    }

    @Test
    @WithMockUser
    public void listAccount_shouldReturnListViewWithAccounts() throws Exception {
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
}
