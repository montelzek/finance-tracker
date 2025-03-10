package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.repository.FinancialGoalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinancialGoalService {

    private final FinancialGoalRepository financialGoalRepository;

    public FinancialGoalService(FinancialGoalRepository financialGoalRepository) {
        this.financialGoalRepository = financialGoalRepository;
    }

    public List<FinancialGoal> findUsersFinancialGoals(Long userId) {
        return financialGoalRepository.findByUserId_OrderByCreatedAt(userId);
    }

    public void save(FinancialGoal financialGoal) {
        financialGoalRepository.save(financialGoal);
    }

    public FinancialGoal findById(Long id) {
        return financialGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financial Goal not find with id: " + id));
    }

    public void deleteById(Long id) {
        financialGoalRepository.deleteById(id);
    }
}
