package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.FinancialGoalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinancialGoalService {

    private final FinancialGoalRepository financialGoalRepository;
    private final UserService userService;

    public FinancialGoalService(FinancialGoalRepository financialGoalRepository, UserService userService) {
        this.financialGoalRepository = financialGoalRepository;
        this.userService = userService;
    }

    public List<FinancialGoal> findUsersFinancialGoals(Long userId) {
        return financialGoalRepository.findByUserId_OrderByCreatedAt(userId);
    }

    public FinancialGoal save(FinancialGoal financialGoal) {
        return financialGoalRepository.save(financialGoal);
    }

    public FinancialGoal findById(Long id) {
        return financialGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financial Goal not find with id: " + id));
    }

    public void deleteById(Long id) {
        financialGoalRepository.deleteById(id);
    }

    public List<FinancialGoal> findTop3Goals(Long userId) {
        return financialGoalRepository.findTop3ByUserId(userId);
    }

    @Transactional
    public void saveFinancialGoal(FinancialGoalDTO financialGoalDTO) {
        if (financialGoalDTO.getId() != null) {
            updateFinancialGoal(financialGoalDTO);
        } else {
            createFinancialGoal(financialGoalDTO);
        }
    }

    @Transactional
    public void createFinancialGoal(FinancialGoalDTO financialGoalDTO) {
        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        FinancialGoal financialGoal = new FinancialGoal(
                financialGoalDTO.getName(),
                financialGoalDTO.getTargetAmount()
        );
        financialGoal.setUser(user);
        financialGoal.setCurrentAmount(BigDecimal.ZERO);
        financialGoal.setIsAchieved(false);

        save(financialGoal);
    }

    @Transactional
    public void updateFinancialGoal(FinancialGoalDTO financialGoalDTO) {
        if (financialGoalDTO.getId() == null) {
            throw new IllegalArgumentException("Budget ID can't be null");
        }

        FinancialGoal financialGoal = findById(financialGoalDTO.getId());

        financialGoal.setName(financialGoalDTO.getName());
        financialGoal.setTargetAmount(financialGoalDTO.getTargetAmount());

        updateAchievementStatus(financialGoal);
        save(financialGoal);
    }

    private void updateAchievementStatus(FinancialGoal financialGoal) {
        if (financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) >= 0 && !financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(true);
        } else if (financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) < 0 && financialGoal.getIsAchieved()) {
            financialGoal.setIsAchieved(false);
        }
    }

    public FinancialGoalDTO convertToDTO(FinancialGoal financialGoal) {
        FinancialGoalDTO financialGoalDTO = new FinancialGoalDTO();
        financialGoalDTO.setId(financialGoal.getId());
        financialGoalDTO.setName(financialGoal.getName());
        financialGoalDTO.setTargetAmount(financialGoal.getTargetAmount());
        return financialGoalDTO;
    }
}
