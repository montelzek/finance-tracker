package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.BudgetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public BudgetService(BudgetRepository budgetRepository, UserService userService, CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    public List<Budget> findUsersBudgets(Long userId) {
        return budgetRepository.findByUserId_OrderByCreatedAt(userId);
    }

    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    public Budget findById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not find with id: " + id));
    }

    public void deleteById(Long id) {
        budgetRepository.deleteById(id);
    }

    public List<Budget> findBudgetsByCategoryAndDate(Long userId, Category category, LocalDate date) {
        return budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, category, date, date);
    }

    public List<Budget> findActiveBudgets(Long userId, LocalDate date) {
        return budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, date, date);
    }

    @Transactional
    public void createBudget(BudgetDTO budgetDTO) {
        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        Category category = categoryService.findById(budgetDTO.getCategoryId());

        Budget budget = new Budget(
                budgetDTO.getName(),
                budgetDTO.getStartDate(),
                budgetDTO.getEndDate(),
                budgetDTO.getBudgetSize()
        );
        budget.setUser(user);
        budget.setCategory(category);
        budget.setBudgetSpent(BigDecimal.ZERO);

        save(budget);
    }

    @Transactional
    public void updateBudget(BudgetDTO budgetDTO) {
        if (budgetDTO.getId() == null) {
            throw new IllegalArgumentException("Budget ID can't be null");
        }

        Budget budget = findById(budgetDTO.getId());
        Category category = categoryService.findById(budgetDTO.getCategoryId());

        budget.setName(budgetDTO.getName());
        budget.setStartDate(budgetDTO.getStartDate());
        budget.setEndDate(budgetDTO.getEndDate());
        budget.setBudgetSize(budgetDTO.getBudgetSize());
        budget.setCategory(category);

        save(budget);
    }

    @Transactional
    public void saveBudget(BudgetDTO budgetDTO) {
        if (budgetDTO.getId() != null) {
            updateBudget(budgetDTO);
        } else {
            createBudget(budgetDTO);
        }
    }
    public BudgetDTO convertToDTO(Budget budget) {
        BudgetDTO budgetDTO = new BudgetDTO();
        budgetDTO.setId(budget.getId());
        budgetDTO.setName(budget.getName());
        budgetDTO.setStartDate(budget.getStartDate());
        budgetDTO.setEndDate(budget.getEndDate());
        budgetDTO.setBudgetSize(budget.getBudgetSize());
        budgetDTO.setCategoryId(budget.getCategory().getId());
        return budgetDTO;
    }
}
