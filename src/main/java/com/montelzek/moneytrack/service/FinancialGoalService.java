package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import java.util.List;

public interface FinancialGoalService {

    List<FinancialGoal> findUsersFinancialGoals(Long userId);

    FinancialGoal save(FinancialGoal financialGoal);

    FinancialGoal findById(Long id);

    void deleteById(Long id);

    List<FinancialGoal> findTop3Goals(Long userId);

    void saveFinancialGoal(FinancialGoalDTO financialGoalDTO);

    void createFinancialGoal(FinancialGoalDTO financialGoalDTO);

    void updateFinancialGoal(FinancialGoalDTO financialGoalDTO);

    FinancialGoalDTO convertToDTO(FinancialGoal financialGoal);
}
