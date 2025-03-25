package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FinancialGoalRepositoryTests {

    @Autowired
    private FinancialGoalRepository financialGoalRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user1;

    @BeforeEach
    void setup() {
        user1 = User.builder()
                .email("user1@gmail.com")
                .password("admin")
                .firstName("Jane")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();
        testEntityManager.persistAndFlush(user1);

        User user2 = User.builder()
                .email("user2@gmail.com")
                .password("password")
                .firstName("John")
                .lastName("Smith")
                .roles(new HashSet<>())
                .build();
        testEntityManager.persistAndFlush(user2);

        // 4 financial goals for user1

        testEntityManager.persist(FinancialGoal.builder()
                .name("Test Goal 1")
                .targetAmount(BigDecimal.valueOf(1000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user1)
                .build()
        );

        testEntityManager.persist(FinancialGoal.builder()
                .name("Test Goal 2")
                .targetAmount(BigDecimal.valueOf(2000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user1)
                .build()
        );

        testEntityManager.persist(FinancialGoal.builder()
                .name("Test Goal 3")
                .targetAmount(BigDecimal.valueOf(3000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user1)
                .build()
        );

        testEntityManager.persist(FinancialGoal.builder()
                .name("Test Goal 4")
                .targetAmount(BigDecimal.valueOf(4000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user1)
                .build()
        );

        // 1 financial goal for user2

        testEntityManager.persist(FinancialGoal.builder()
                .name("Test Goal 5")
                .targetAmount(BigDecimal.valueOf(5000))
                .currentAmount(BigDecimal.ZERO)
                .isAchieved(false)
                .user(user2)
                .build()
        );

        testEntityManager.flush();
    }

    @Test
    void testFindByUserId_OrderByCreatedAt_whenUserIdIsValid_shouldReturnFinancialGoalsInOrder() {
        // Act
        List<FinancialGoal> financialGoalList = financialGoalRepository.findByUserId_OrderByCreatedAt(user1.getId());

        // Assert
        assertThat(financialGoalList).isNotNull();
        assertThat(financialGoalList).hasSize(4);
        for (FinancialGoal financialGoal : financialGoalList) {
            assertThat(financialGoal.getUser().getId()).isEqualTo(user1.getId());
        }
        assertThat(financialGoalList).isSortedAccordingTo(Comparator.comparing(FinancialGoal::getCreatedAt));
    }

    @Test
    void testFindByUserId_OrderByCreatedAt_whenUserIdIsNotValid_shouldReturnEmptyList() {
        // Act
        List<FinancialGoal> financialGoalList = financialGoalRepository.findByUserId_OrderByCreatedAt(999L);

        // Assert
        assertThat(financialGoalList).isEmpty();
    }
}
