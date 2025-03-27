package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.FinancialGoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FinancialGoalServiceImplTest {

    @Mock
    private FinancialGoalRepository financialGoalRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private FinancialGoalServiceImpl financialGoalService;

    private User user;
    private FinancialGoal financialGoal;
    private FinancialGoalDTO financialGoalDTO;

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();

        financialGoal = FinancialGoal.builder()
                .id(1L)
                .name("Test Financial Goal")
                .targetAmount(BigDecimal.valueOf(1000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user)
                .build();

        financialGoalDTO = FinancialGoalDTO.builder()
                .id(1L)
                .name("Test Financial Goal DTO")
                .targetAmount(BigDecimal.valueOf(2000))
                .build();
    }

    @Test
    public void findUsersFinancialGoals_existingUserId_shouldReturnFinancialGoalsList() {
        // Arrange
        when(financialGoalRepository.findByUserId_OrderByCreatedAt(user.getId())).thenReturn(List.of(financialGoal));

        // Act
        List<FinancialGoal> result = financialGoalService.findUsersFinancialGoals(user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getId()).isEqualTo(user.getId());
        verify(financialGoalRepository).findByUserId_OrderByCreatedAt(user.getId());
    }

    @Test
    public void findUsersFinancialGoals_nonExistingUserId_shouldReturnEmptyFinancialGoalsList() {
        // Arrange
        when(financialGoalRepository.findByUserId_OrderByCreatedAt(-1L)).thenReturn(List.of());

        // Act
        List<FinancialGoal> result = financialGoalService.findUsersFinancialGoals(-1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(financialGoalRepository).findByUserId_OrderByCreatedAt(-1L);
    }

    @Test
    public void save_validFinancialGoal_shouldSaveFinancialGoal() {
        // Arrange
        when(financialGoalRepository.save(financialGoal)).thenReturn(financialGoal);

        // Act
        FinancialGoal savedFinancialGoal = financialGoalService.save(financialGoal);

        // Assert
        assertThat(savedFinancialGoal).isEqualTo(financialGoal);
        verify(financialGoalRepository).save(financialGoal);
    }

    @Test
    public void findById_existingId_shouldReturnFinancialGoal() {
        // Arrange
        Long financialGoalId = financialGoal.getId();
        when(financialGoalRepository.findById(financialGoalId)).thenReturn(Optional.of(financialGoal));

        // Act
        FinancialGoal foundFinancialGoal = financialGoalService.findById(financialGoalId);

        // Assert
        assertThat(foundFinancialGoal).isNotNull();
        assertThat(foundFinancialGoal.getName()).isEqualTo(financialGoal.getName());
        assertThat(foundFinancialGoal.getTargetAmount()).isEqualTo(financialGoal.getTargetAmount());
        assertThat(foundFinancialGoal.getCurrentAmount()).isEqualTo(financialGoal.getCurrentAmount());
        assertThat(foundFinancialGoal.getIsAchieved()).isEqualTo(financialGoal.getIsAchieved());
        verify(financialGoalRepository).findById(financialGoalId);
    }

    @Test
    public void findById_nonExistingId_shouldThrowRuntimeException() {
        // Arrange
        Long nonExistingId = -1L;
        when(financialGoalRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> financialGoalService.findById(nonExistingId))
                .withMessage("Financial Goal not find with id: " + nonExistingId);
        verify(financialGoalRepository).findById(nonExistingId);
    }

    @Test
    public void deleteById_shouldCallRepositoryDelete() {
        // Arrange
        Long testId = 1L;
        // Act
        financialGoalService.deleteById(testId);
        //Assert
        verify(financialGoalRepository).deleteById(testId);
    }
}
