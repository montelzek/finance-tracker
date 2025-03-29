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
class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserService userService;
    private final CategoryService categoryService;

    public BudgetServiceImpl(BudgetRepository budgetRepository, UserService userService, CategoryService categoryService) {
        this.budgetRepository = budgetRepository;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @Override
    public List<Budget> findUsersBudgets(Long userId) {
        return budgetRepository.findByUserId_OrderByCreatedAt(userId);
    }

    @Override
    public Budget save(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Override
    public Budget findById(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not find with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        budgetRepository.deleteById(id);
    }

    @Override
    public List<Budget> findBudgetsByCategoryAndDate(Long userId, Category category, LocalDate date) {
        return budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                userId, category, date, date);
    }

    @Override
    public List<Budget> findActiveBudgets(Long userId, LocalDate date) {
        return budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, date, date);
    }

    @Override
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

    @Override
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

    @Override
    @Transactional
    public void saveBudget(BudgetDTO budgetDTO) {
        if (budgetDTO.getId() != null) {
            updateBudget(budgetDTO);
        } else {
            createBudget(budgetDTO);
        }
    }

    @Override
    public BudgetDTO convertToDTO(Budget budget) {
        return BudgetDTO.builder()
                .id(budget.getId())
                .name(budget.getName())
                .startDate(budget.getStartDate())
                .endDate(budget.getEndDate())
                .budgetSize(budget.getBudgetSize())
                .categoryId(budget.getCategory().getId())
                .build();
    }
}