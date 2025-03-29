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
class FinancialGoalServiceImpl implements FinancialGoalService {

    private final FinancialGoalRepository financialGoalRepository;
    private final UserService userService;

    public FinancialGoalServiceImpl(FinancialGoalRepository financialGoalRepository, UserService userService) {
        this.financialGoalRepository = financialGoalRepository;
        this.userService = userService;
    }

    @Override
    public List<FinancialGoal> findUsersFinancialGoals(Long userId) {
        return financialGoalRepository.findByUserId_OrderByCreatedAt(userId);
    }

    @Override
    public FinancialGoal save(FinancialGoal financialGoal) {
        return financialGoalRepository.save(financialGoal);
    }

    @Override
    public FinancialGoal findById(Long id) {
        return financialGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Financial Goal not find with id: " + id));
    }

    @Override
    public void deleteById(Long id) {
        financialGoalRepository.deleteById(id);
    }

    @Override
    public List<FinancialGoal> findTop3Goals(Long userId) {
        return financialGoalRepository.findTop3ByUserId(userId);
    }

    @Override
    @Transactional
    public void saveFinancialGoal(FinancialGoalDTO financialGoalDTO) {
        if (financialGoalDTO.getId() != null) {
            updateFinancialGoal(financialGoalDTO);
        } else {
            createFinancialGoal(financialGoalDTO);
        }
    }

    @Override
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

    @Override
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

    @Override
    public FinancialGoalDTO convertToDTO(FinancialGoal financialGoal) {
        return FinancialGoalDTO.builder()
                .id(financialGoal.getId())
                .name(financialGoal.getName())
                .targetAmount(financialGoal.getTargetAmount())
                .build();
    }
}