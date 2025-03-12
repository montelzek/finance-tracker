package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public List<Budget> findUsersBudgets(Long userId) {
        return budgetRepository.findByUserId_OrderByCreatedAt(userId);
    }

    public void save(Budget budget) {
        budgetRepository.save(budget);
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
}
