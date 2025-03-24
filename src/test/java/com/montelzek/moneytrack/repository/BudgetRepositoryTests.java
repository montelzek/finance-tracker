package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Budget;
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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BudgetRepositoryTests {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User testUser;
    private Budget budget1;
    private Budget budget2;
    private Budget budget3;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("testuser@gmail.com")
                .password("admin")
                .firstName("Jane")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();

        testEntityManager.persistAndFlush(testUser);

        budget1 = Budget.builder()
                .name("Test Budget 1")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .budgetSize(BigDecimal.valueOf(2000))
                .user(testUser)
                .build();

        budget2 = Budget.builder()
                .name("Test Budget 2")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(1))
                .budgetSize(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        budget3 = Budget.builder()
                .name("Test Budget 3")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(3))
                .budgetSize(BigDecimal.valueOf(3000))
                .user(testUser)
                .build();
    }

    @Test
    void testFindByUserIdOrderByCreatedAt_shouldReturnBudgetsInOrder() {

        // Arrange
        testEntityManager.persist(budget2);
        testEntityManager.persist(budget1);
        testEntityManager.persist(budget3);
        testEntityManager.flush();

        // Act
        List<Budget> budgetList = budgetRepository.findByUserId_OrderByCreatedAt(testUser.getId());

        // Assert
        assertThat(budgetList).isNotNull();
        assertThat(budgetList).hasSize(3);
        assertThat(budgetList.get(0).getName()).isEqualTo(budget2.getName());
        assertThat(budgetList.get(1).getName()).isEqualTo(budget1.getName());
        assertThat(budgetList.get(2).getName()).isEqualTo(budget3.getName());
    }
}
