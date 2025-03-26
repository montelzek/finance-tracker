package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Account;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
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
                .id(null)
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

    @Test
    public void save_validBudget_shouldSaveBudget() {
        // Arrange
        when(budgetRepository.save(budget)).thenReturn(budget);

        // Act
        Budget savedBudget = budgetService.save(budget);

        // Assert
        assertThat(savedBudget).isEqualTo(budget);
        verify(budgetRepository).save(budget);
    }

    @Test
    public void findById_existingId_shouldReturnBudget() {
        // Arrange
        Long budgetId = budget.getId();
        when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(budget));

        // Act
        Budget foundBudget = budgetService.findById(budgetId);

        // Assert
        assertThat(foundBudget).isNotNull();
        assertThat(foundBudget.getName()).isEqualTo(budget.getName());
        assertThat(foundBudget.getBudgetSize()).isEqualTo(budget.getBudgetSize());
        assertThat(foundBudget.getStartDate()).isEqualTo(budget.getStartDate());
        assertThat(foundBudget.getEndDate()).isEqualTo(budget.getEndDate());
        verify(budgetRepository).findById(budgetId);
    }

    @Test
    public void findById_nonExistingId_shouldThrowRuntimeException() {
        // Arrange
        Long nonExistingId = -1L;
        when(budgetRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> budgetService.findById(nonExistingId))
                .withMessage("Budget not find with id: " + nonExistingId);
        verify(budgetRepository).findById(nonExistingId);
    }

    @Test
    public void deleteById_shouldCallRepositoryDelete() {
        // Arrange
        Long testId = 1L;
        // Act
        budgetService.deleteById(testId);
        //Assert
        verify(budgetRepository).deleteById(testId);
    }

    @Test
    public void findBudgetsByCategoryAndDate_shouldReturnListOfMatchingBudgets() {
        // Arrange
        Budget budget1 = Budget.builder()
                .user(user)
                .category(category)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(5))
                .build();
        Budget budget2 = Budget.builder()
                .user(user)
                .category(category)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(5))
                .build();
        when(budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), category, LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)
        )).thenReturn(List.of(budget1, budget2));

        // Act
        List<Budget> foundBudgets = budgetService.findBudgetsByCategoryAndDate(
                user.getId(), category, LocalDate.now().plusDays(3));

        // Assert
        assertThat(foundBudgets).hasSize(2);
        assertThat(foundBudgets.get(0).getCategory().getName()).isEqualTo(budget1.getCategory().getName());
        assertThat(foundBudgets.get(1).getCategory().getName()).isEqualTo(budget1.getCategory().getName());
        verify(budgetRepository).findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), category, LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)
        );
    }

    @Test
    public void findBudgetsByCategoryAndDate_noMatchingBudgets_shouldReturnEmptyListOfBudgets() {
        // Arrange
        when(budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), category, LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)
        )).thenReturn(List.of());

        // Act
        List<Budget> foundBudgets = budgetService.findBudgetsByCategoryAndDate(
                user.getId(), category, LocalDate.now().plusDays(3));

        // Assert
        assertThat(foundBudgets).isNotNull();
        assertThat(foundBudgets).isEmpty();
        verify(budgetRepository).findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), category, LocalDate.now().plusDays(3), LocalDate.now().plusDays(3)
        );
    }

    @Test
    public void findActiveBudgets_fourActiveBudgets_shouldReturn4Budgets() {
        // Arrange
        Budget budget1 = Budget.builder()
                .user(user)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .build();
        Budget budget2 = Budget.builder()
                .user(user)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .build();
        Budget budget3 = Budget.builder()
                .user(user)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .build();
        Budget budget4 = Budget.builder()
                .user(user)
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .build();
        when(budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), LocalDate.now(), LocalDate.now()
        )).thenReturn(List.of(budget1, budget2, budget3, budget4));

        // Act
        List<Budget> activeBudgets = budgetService.findActiveBudgets(user.getId(), LocalDate.now());

        // Assert
        assertThat(activeBudgets).hasSize(4);
        verify(budgetRepository).findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), LocalDate.now(), LocalDate.now()
        );
    }

    @Test
    public void findActiveBudgets_zeroActiveBudgets_shouldReturnEmptyList() {
        // Arrange
        when(budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), LocalDate.now(), LocalDate.now()
        )).thenReturn(List.of());

        // Act
        List<Budget> activeBudgets = budgetService.findActiveBudgets(user.getId(), LocalDate.now());

        // Assert
        assertThat(activeBudgets).isNotNull();
        assertThat(activeBudgets).isEmpty();
        verify(budgetRepository).findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user.getId(), LocalDate.now(), LocalDate.now()
        );
    }

    @Test
    public void saveBudget_idIsNull_shouldCreateNewBudget() {
        // Arrange
        BudgetDTO newBudgetDTO = BudgetDTO.builder()
                .id(null)
                .name("New Budget")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .budgetSize(BigDecimal.valueOf(1000))
                .categoryId(category.getId())
                .build();

        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(categoryService.findById(category.getId())).thenReturn(category);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // Act
        budgetService.saveBudget(newBudgetDTO);

        // Assert
        verify(userService).getCurrentUserId();
        verify(userService).findById(1L);
        verify(categoryService).findById(category.getId());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    public void saveBudget_idIsNotNull_shouldUpdateBudget() {
        // Arrange
        BudgetDTO existingBudgetDTO = BudgetDTO.builder()
                .id(1L)
                .name("Update Budget")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .budgetSize(BigDecimal.valueOf(1000))
                .categoryId(category.getId())
                .build();

        when(budgetRepository.findById(1L)).thenReturn(Optional.of(budget));
        when(budgetRepository.save(budget)).thenReturn(budget);

        // Act
        budgetService.saveBudget(existingBudgetDTO);

        // Assert
        assertThat(budget.getName()).isEqualTo("Update Budget");
        verify(budgetRepository).findById(1L);
        verify(budgetRepository).save(budget);
    }

    @Test
    public void createBudget_shouldCreateNewBudget() {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(categoryService.findById(category.getId())).thenReturn(category);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        // Act
        budgetService.saveBudget(budgetDTO);

        // Assert
        verify(userService).getCurrentUserId();
        verify(userService).findById(1L);
        verify(categoryService).findById(category.getId());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    public void createBudget_userNotFound_shouldThrowException() {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> budgetService.createBudget(budgetDTO))
                .withMessage("User not found");
    }
}
