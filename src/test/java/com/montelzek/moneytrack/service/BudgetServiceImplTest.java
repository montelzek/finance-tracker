package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private UserService userService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private User user;
    private Category category;
    private Budget budget;
    private BudgetDTO budgetDTO;

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();

        category = Category.builder()
                .id(1)
                .name("Groceries")
                .type("EXPENSE")
                .build();

        budget = Budget.builder()
                .id(1L)
                .name("Test Budget")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(15))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.ZERO)
                .user(user)
                .category(category)
                .build();

        budgetDTO = BudgetDTO.builder()
                .id(1L)
                .name("Test Budget DTO")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(20))
                .budgetSize(BigDecimal.valueOf(1500))
                .categoryId(category.getId())
                .build();
    }

    @Test
    public void findUsersBudgets_existingUserId_shouldReturnBudgetList() {
        // Arrange
        when(budgetRepository.findByUserId_OrderByCreatedAt(user.getId())).thenReturn(List.of(budget));

        // Act
        List<Budget> result = budgetService.findUsersBudgets(user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getId()).isEqualTo(user.getId());
        verify(budgetRepository).findByUserId_OrderByCreatedAt(user.getId());
    }

    @Test
    public void findUsersBudgets_nonExistingUserId_shouldReturnEmptyBudgetsList() {
        // Arrange
        when(budgetRepository.findByUserId_OrderByCreatedAt(-1L)).thenReturn(List.of());

        // Act
        List<Budget> result = budgetService.findUsersBudgets(-1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(budgetRepository).findByUserId_OrderByCreatedAt(-1L);
    }
}
