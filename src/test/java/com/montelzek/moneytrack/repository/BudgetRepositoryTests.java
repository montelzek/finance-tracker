package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Category;
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

    private User user1;
    private User user2;
    private Category category1;
    private Category category2;
    private Budget budget1;
    private Budget budget2;
    private Budget budget3;
    private Budget budget4;
    private Budget budget5;
    private Budget budget6;
    private Budget budget7;
    private Budget budget8;
    private Budget budget9;

    @BeforeEach
    void setup() {
        category1 = Category.builder()
                .name("Groceries")
                .type("EXPENSE")
                .build();
        testEntityManager.persistAndFlush(category1);

        category2 = Category.builder()
                .name("Entertainment")
                .type("EXPENSE")
                .build();
        testEntityManager.persistAndFlush(category2);

        user1 = User.builder()
                .email("user1@gmail.com")
                .password("admin")
                .firstName("Jane")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();
        testEntityManager.persistAndFlush(user1);

        user2 = User.builder()
                .email("user2@gmail.com")
                .password("password")
                .firstName("John")
                .lastName("Smith")
                .roles(new HashSet<>())
                .build();
        testEntityManager.persistAndFlush(user2);

        // user1 category1

        budget1 = Budget.builder()
                .name("Test Budget 1")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.ZERO)
                .category(category1)
                .user(user1)
                .build();

        budget2 = Budget.builder()
                .name("Test Budget 2")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(1))
                .budgetSize(BigDecimal.valueOf(1000))
                .budgetSpent(BigDecimal.valueOf(300))
                .category(category1)
                .user(user1)
                .build();

        budget3 = Budget.builder()
                .name("Test Budget 3")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(3))
                .budgetSize(BigDecimal.valueOf(3000))
                .budgetSpent(BigDecimal.valueOf(500))
                .category(category1)
                .user(user1)
                .build();

        budget4 = Budget.builder()
                .name("Test Budget 4")
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(10))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.ZERO)
                .category(category1)
                .user(user1)
                .build();

        // user1 category2

        budget5 = Budget.builder()
                .name("Test Budget 5")
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusWeeks(10))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.ZERO)
                .category(category2)
                .user(user1)
                .build();

        // user2 category1

        budget6 = Budget.builder()
                .name("Test Budget 5")
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(10))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.ZERO)
                .category(category1)
                .user(user2)
                .build();

        budget7 = Budget.builder()
                .name("Test Budget 6")
                .startDate(LocalDate.now().minusDays(15))
                .endDate(LocalDate.now().plusDays(15))
                .budgetSize(BigDecimal.valueOf(1100))
                .budgetSpent(BigDecimal.ZERO)
                .category(category1)
                .user(user1)
                .build();

        budget8 = Budget.builder()
                .name("Test Budget 7")
                .startDate(LocalDate.now().minusDays(20))
                .endDate(LocalDate.now().plusDays(20))
                .budgetSize(BigDecimal.valueOf(1300))
                .budgetSpent(BigDecimal.ZERO)
                .category(category2)
                .user(user1)
                .build();

        budget9 = Budget.builder()
                .name("Test Budget 8")
                .startDate(LocalDate.now().minusDays(35))
                .endDate(LocalDate.now().plusDays(15))
                .budgetSize(BigDecimal.valueOf(1600))
                .budgetSpent(BigDecimal.ZERO)
                .category(category1)
                .user(user1)
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
        List<Budget> budgetList = budgetRepository.findByUserId_OrderByCreatedAt(user1.getId());

        // Assert
        assertThat(budgetList).isNotNull();
        assertThat(budgetList).hasSize(3);
        assertThat(budgetList.get(0).getName()).isEqualTo(budget2.getName());
        assertThat(budgetList.get(1).getName()).isEqualTo(budget1.getName());
        assertThat(budgetList.get(2).getName()).isEqualTo(budget3.getName());
    }

    @Test
    void testFindByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual_shouldReturnMatchingBudgets() {
        // Arrange
        testEntityManager.persist(budget1);
        testEntityManager.persist(budget2);
        testEntityManager.persist(budget3);
        testEntityManager.persist(budget4);
        testEntityManager.persist(budget6);
        testEntityManager.flush();
        LocalDate now = LocalDate.now();
        LocalDate endQuery = now.plusDays(7);

        // Act
        List<Budget> result = budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user1.getId(), category1, now, endQuery);

        // Assert
        assertThat(result).hasSize(3);
        for (Budget b : result) {
            assertThat(b.getUser().getId()).isEqualTo(user1.getId());
            assertThat(b.getCategory()).isEqualTo(category1);
            assertThat(b.getStartDate()).isBeforeOrEqualTo(now);
            assertThat(b.getEndDate()).isAfterOrEqualTo(endQuery);
        }
    }

    @Test
    void testFindByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual_noMatch() {
        // Arrange
        testEntityManager.persistAndFlush(budget1);
        LocalDate now = LocalDate.now();
        LocalDate startQuery = now.plusDays(15);
        LocalDate endQuery = now.plusDays(20);

        // Act
        List<Budget> result = budgetRepository.findByUserIdAndCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user1.getId(), category1, startQuery, endQuery);

        // Assert
        assertThat(result).isEmpty();
    }


    @Test
    void testFindTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual_shouldReturnTop4MatchingBudgets() {
        // Arrange
        testEntityManager.persist(budget1);
        testEntityManager.persist(budget2);
        testEntityManager.persist(budget4);
        testEntityManager.persist(budget5);
        testEntityManager.persist(budget7);
        testEntityManager.flush();
        LocalDate now = LocalDate.now();
        LocalDate endQuery = now.plusDays(7);

        // Act
        List<Budget> result = budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user1.getId(), now, endQuery);

        // Assert
        assertThat(result).hasSize(4);
        for (Budget b : result) {
            assertThat(b.getUser().getId()).isEqualTo(user1.getId());
            assertThat(b.getStartDate()).isBeforeOrEqualTo(now);
            assertThat(b.getEndDate()).isAfterOrEqualTo(endQuery);
        }
    }

    @Test
    void testFindTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual_noMatch() {
        // Arrange
        testEntityManager.persist(budget1);
        testEntityManager.persist(budget2);
        testEntityManager.persist(budget4);
        LocalDate startQuery = LocalDate.now().plusDays(100);
        LocalDate endQuery = LocalDate.now().plusDays(110);

        // Act
        List<Budget> result = budgetRepository.findTop4ByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                user1.getId(), startQuery, endQuery);

        // Assert
        assertThat(result).isEmpty();
    }

}
