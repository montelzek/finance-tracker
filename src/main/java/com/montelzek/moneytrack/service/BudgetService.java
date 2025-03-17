package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.BudgetDTO;
import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
import java.time.LocalDate;
import java.util.List;

public interface BudgetService {

    List<Budget> findUsersBudgets(Long userId);

    Budget save(Budget budget);

    Budget findById(Long id);

    void deleteById(Long id);

    List<Budget> findBudgetsByCategoryAndDate(Long userId, Category category, LocalDate date);

    List<Budget> findActiveBudgets(Long userId, LocalDate date);

    void createBudget(BudgetDTO budgetDTO);

    void updateBudget(BudgetDTO budgetDTO);

    void saveBudget(BudgetDTO budgetDTO);

    BudgetDTO convertToDTO(Budget budget);
}
