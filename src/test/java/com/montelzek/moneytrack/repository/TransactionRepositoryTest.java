package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User user;
    private Account account;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    public void setup() {
        user = User.builder()
                .email("test@example.com")
                .password("admin")
                .build();
        testEntityManager.persist(user);

        account = Account.builder()
                .user(user)
                .name("Test Account")
                .accountType(Account.AccountType.CASH)
                .balance(BigDecimal.ZERO)
                .currency(Account.Currency.USD)
                .build();
        testEntityManager.persist(account);

        incomeCategory = Category.builder()
                .type("INCOME")
                .name("Salary")
                .build();
        testEntityManager.persist(incomeCategory);

        expenseCategory = Category.builder()
                .type("EXPENSE")
                .name("Groceries")
                .build();
        testEntityManager.persist(expenseCategory);

        testEntityManager.flush();
    }

    @Test
    public void testFindByAccount_User_Id_ShouldReturnPagedTransactions() {
        // Arrange
        for (int i = 0; i < 10; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .account(account)
                    .category(i % 2 == 0 ? incomeCategory : expenseCategory)
                    .build();
            testEntityManager.persist(transaction);
        }
        testEntityManager.flush();

        // Act
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> transactionPage = transactionRepository
                .findByAccount_User_Id_OrderByDateDescCreatedAtDesc(user.getId(), pageable);

        // Assert
        assertThat(transactionPage).isNotNull();
        assertThat(transactionPage.getContent()).hasSize(5);
        assertThat(transactionPage.getContent().getFirst().getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void testFindTop6ByAccount_User_Id_ShouldReturnSixMostRecentTransactions() {
        // Arrange
        for (int i = 0; i < 7; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .account(account)
                    .category(i % 2 == 0 ? incomeCategory : expenseCategory)
                    .build();
            testEntityManager.persist(transaction);
        }
        testEntityManager.flush();

        // Act
        List<Transaction> recentTransactions = transactionRepository
                .findTop6ByAccount_User_Id_OrderByDateDesc(user.getId());

        // Assert
        assertThat(recentTransactions).hasSize(6);
        assertThat(recentTransactions.getFirst().getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void testFindIncomeTransactionsFromPastMonth_ShouldReturnOnlyIncomeTransactionsFromLast30Days() {
        // Arrange
        for (int i = 0; i < 10; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .account(account)
                    .category(i % 2 == 0 ? incomeCategory : expenseCategory)
                    .build();
            testEntityManager.persist(transaction);
        }
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .date(LocalDate.now().minusDays(31))
                .account(account)
                .category(incomeCategory)
                .build();
        testEntityManager.persist(transaction);
        testEntityManager.flush();

        // Act
        List<Transaction> foundIncomeTransactions = transactionRepository
                .findIncomeTransactionsFromPastMonth(user.getId());

        // Assert
        for (Transaction t : foundIncomeTransactions) {
            assertThat(t.getCategory().getType()).isEqualTo("INCOME");
        }
        assertThat(foundIncomeTransactions).hasSize(5);
    }

    @Test
    public void testFindExpenseTransactionsFromPastMonth_ShouldReturnOnlyExpenseTransactionsFromLast30Days() {
        // Arrange
        for (int i = 0; i < 10; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .account(account)
                    .category(i % 2 == 0 ? incomeCategory : expenseCategory)
                    .build();
            testEntityManager.persist(transaction);
        }
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .date(LocalDate.now().minusDays(31))
                .account(account)
                .category(expenseCategory)
                .build();
        testEntityManager.persist(transaction);
        testEntityManager.flush();

        // Act
        List<Transaction> foundExpenseTransactions = transactionRepository
                .findExpenseTransactionsFromPastMonth(user.getId());

        // Assert
        for (Transaction t : foundExpenseTransactions) {
            assertThat(t.getCategory().getType()).isEqualTo("EXPENSE");
        }
        assertThat(foundExpenseTransactions).hasSize(5);
    }

    @Test
    public void testFindTransactionsFromLastSixMonths_shouldReturnTransactionsFromLast6Months() {
        // Arrange
        for (int i = 0; i < 5; i++) {
            Transaction transaction = Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .account(account)
                    .category(i % 2 == 0 ? incomeCategory : expenseCategory)
                    .build();
            testEntityManager.persist(transaction);
        }
        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(500))
                .date(LocalDate.now().minusMonths(7))
                .account(account)
                .category(expenseCategory)
                .build();
        testEntityManager.persist(transaction);
        testEntityManager.flush();

        // Act
        List<Transaction> transactionList = transactionRepository
                .findTransactionsFromLastSixMonths(user.getId(), LocalDate.now().minusMonths(6));

        // Assert
        assertThat(transactionList).hasSize(5);
        for (Transaction t : transactionList) {
            assertThat(t.getDate()).isAfter(LocalDate.now().minusMonths(6).minusDays(1));
        }
    }
}
