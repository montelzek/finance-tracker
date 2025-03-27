package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.model.Account;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    public void findTop3Goals_given3Goals_shouldReturn3Goals() {
        // Arrange
        FinancialGoal financialGoal1 = FinancialGoal.builder()
                .name("Goal 1")
                .user(user)
                .build();
        FinancialGoal financialGoal2 = FinancialGoal.builder()
                .name("Goal 2")
                .user(user)
                .build();
        FinancialGoal financialGoal3 = FinancialGoal.builder()
                .name("Goal 3")
                .user(user)
                .build();
        when(financialGoalRepository.findTop3ByUserId(user.getId()))
                .thenReturn(List.of(financialGoal1, financialGoal2, financialGoal3));

        // Act
        List<FinancialGoal> financialGoals = financialGoalService.findTop3Goals(user.getId());

        // Assert
        assertThat(financialGoals).hasSize(3);
        verify(financialGoalRepository).findTop3ByUserId(user.getId());
    }

    @Test
    public void findTop3Goals_zeroGoals_shouldReturnEmptyList() {
        // Arrange
        when(financialGoalRepository.findTop3ByUserId(user.getId()))
                .thenReturn(List.of());

        // Act
        List<FinancialGoal> financialGoals = financialGoalService.findTop3Goals(user.getId());

        // Assert
        assertThat(financialGoals).isNotNull();
        assertThat(financialGoals).isEmpty();
        verify(financialGoalRepository).findTop3ByUserId(user.getId());
    }

    @Test
    public void saveFinancialGoal_idIsNull_shouldCreateNewFinancialGoal() {
        // Arrange
        FinancialGoalDTO newFinancialGoalDTO = FinancialGoalDTO.builder()
                .id(null)
                .name("New Financial Goal")
                .targetAmount(BigDecimal.valueOf(1000))
                .build();

        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(financialGoalRepository.save(any(FinancialGoal.class))).thenReturn(financialGoal);

        // Act
        financialGoalService.saveFinancialGoal(newFinancialGoalDTO);

        // Assert
        verify(userService).getCurrentUserId();
        verify(userService).findById(1L);
        verify(financialGoalRepository).save(any(FinancialGoal.class));
    }

    @Test
    public void saveFinancialGoal_idIsNotNull_shouldUpdateFinancialGoal() {
        // Arrange
        FinancialGoalDTO existingFinancialGoalDTO = FinancialGoalDTO.builder()
                .id(1L)
                .name("Update Financial Goal")
                .targetAmount(BigDecimal.valueOf(2000))
                .build();

        when(financialGoalRepository.findById(1L)).thenReturn(Optional.of(financialGoal));
        when(financialGoalRepository.save(financialGoal)).thenReturn(financialGoal);

        // Act
        financialGoalService.saveFinancialGoal(existingFinancialGoalDTO);

        // Assert
        assertThat(financialGoal.getName()).isEqualTo("Update Financial Goal");
        verify(financialGoalRepository).findById(1L);
        verify(financialGoalRepository).save(financialGoal);
    }

    @Test
    public void createFinancialGoal_ShouldCreateNewFinancialGoal() {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(financialGoalRepository.save(any(FinancialGoal.class))).thenReturn(financialGoal);

        // Act
        financialGoalService.createFinancialGoal(financialGoalDTO);

        // Assert
        verify(userService).getCurrentUserId();
        verify(userService).findById(1L);
        verify(financialGoalRepository).save(any(FinancialGoal.class));
    }

    @Test
    public void createFinancialGoal_userNotFound_shouldThrowException() {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> financialGoalService.createFinancialGoal(financialGoalDTO))
                .withMessage("User not found");
    }

    @Test
    public void updateFinancialGoal_shouldUpdateExistingFinancialGoal() {
        // Arrange
        when(financialGoalRepository.findById(1L)).thenReturn(Optional.of(financialGoal));
        when(financialGoalRepository.save(financialGoal)).thenReturn(financialGoal);

        financialGoalDTO.setName("Updated Financial Goal Name");

        // Act
        financialGoalService.updateFinancialGoal(financialGoalDTO);

        // Assert
        assertThat(financialGoal.getName()).isEqualTo("Updated Financial Goal Name");
        verify(financialGoalRepository).save(financialGoal);
    }

    @Test
    public void updateFinancialGoal_idIsNull_shouldThrowException() {
        // Arrange
        financialGoalDTO.setId(null);

        // Act & Assert
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> financialGoalService.updateFinancialGoal(financialGoalDTO))
                .withMessage("Budget ID can't be null");
    }


}
