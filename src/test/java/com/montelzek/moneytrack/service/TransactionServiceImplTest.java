package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.FinancialGoalDTO;
import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.*;
import com.montelzek.moneytrack.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BudgetService budgetService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private AccountService accountService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private FinancialGoalService financialGoalService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Transaction transaction;
    private TransactionDTO transactionDTO;
    private Account account;
    private Category category;

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();

        category = Category.builder()
                .id(1)
                .build();

        account = Account.builder()
                .id(1L)
                .user(user)
                .balance(BigDecimal.valueOf(5000))
                .currency(Account.Currency.USD)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .account(account)
                .amount(BigDecimal.valueOf(1000))
                .date(LocalDate.now())
                .category(category)
                .build();
    }

    @Test
    public void findAccountsTransactions_noFilters_shouldReturnPageOfTransactions() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        Page<Transaction> expectedPage = new PageImpl<>(transactions, pageable, transactions.size());

        when(transactionRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<Transaction> result = transactionService.findTransactions(
                userId, null, null, null, null, null, pageable);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void findAccountsTransactions_noFilters_transactionsNotExists_shouldReturnEmptyPage() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> expectedPage = Page.empty();

        when(transactionRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(expectedPage);

        // Act
        Page<Transaction> result = transactionService.findTransactions(
                userId, null, null, null, null, null, pageable);

        // Assert
        assertThat(result).isEmpty();
        verify(transactionRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void getRecentTransactions_ShouldReturnSixMostRecentTransactions() {
        // Arrange
        List<Transaction> mockTransactions = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            mockTransactions.add(Transaction.builder()
                    .amount(BigDecimal.valueOf(100 + i))
                    .date(LocalDate.now().minusDays(i))
                    .build());
        }
        when(transactionRepository.findTop6ByAccount_User_Id_OrderByDateDesc(user.getId()))
                .thenReturn(mockTransactions);

        // Act
        List<Transaction> result = transactionService.getRecentTransactions(user.getId());

        // Assert
        assertThat(result).hasSize(6);
        assertThat(result.getFirst().getDate()).isEqualTo(LocalDate.now());
    }

    @Test
    public void findById_existingId_shouldReturnTransaction() {
        // Arrange
        Long transactionId = transaction.getId();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));

        // Act
        Transaction foundTransaction = transactionService.findById(transactionId);

        // Assert
        assertThat(foundTransaction).isNotNull();
        assertThat(foundTransaction.getId()).isEqualTo(transaction.getId());
        assertThat(foundTransaction.getAmount()).isEqualTo(transaction.getAmount());
        assertThat(foundTransaction.getDate()).isEqualTo(transaction.getDate());
        verify(transactionRepository).findById(transactionId);
    }

    @Test
    public void findById_nonExistingId_shouldThrowRuntimeException() {
        // Arrange
        Long nonExistingId = -1L;
        when(transactionRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> transactionService.findById(nonExistingId))
                .withMessage("Transaction not found with id: " + nonExistingId);
        verify(transactionRepository).findById(nonExistingId);
    }

    @Test
    public void deleteById_incomeTransaction_shouldRevertBalance_andDeleteTransaction() {
        // Arrange
        category.setType("INCOME");
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));

        // Act
        transactionService.deleteById(transaction.getId());

        // Assert
        assertThat(account.getBalance()).isEqualTo("4000");
        verify(transactionRepository).findById(transaction.getId());
        verify(transactionRepository).deleteById(transaction.getId());
        verify(accountService).save(account);
    }

    @Test
    public void deleteById_expenseTransaction_shouldRevertBalanceAndBudget_andDeleteTransaction() {
        // Arrange
        category.setName("Groceries");
        category.setType("EXPENSE");

        Budget budget = Budget.builder()
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(15))
                .budgetSize(BigDecimal.valueOf(2000))
                .budgetSpent(BigDecimal.valueOf(1500))
                .user(user)
                .category(category)
                .build();

        transaction.setBudget(budget);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(exchangeRateService.convertToUSD("USD", BigDecimal.valueOf(1000)))
                .thenReturn(BigDecimal.valueOf(1000));

        // Act
        transactionService.deleteById(transaction.getId());

        // Assert
        assertThat(account.getBalance()).isEqualTo("6000");
        assertThat(budget.getBudgetSpent()).isEqualTo("500");
        verify(transactionRepository).findById(transaction.getId());
        verify(transactionRepository).deleteById(transaction.getId());
        verify(accountService).save(account);
    }

    @Test
    public void deleteById_financialGoalTransaction_shouldRevertBalanceAndGoal_andDeleteTransaction() {
        // Arrange
        category.setType("FINANCIAL_GOAL");

        FinancialGoal financialGoal = FinancialGoal.builder()
                .targetAmount(BigDecimal.valueOf(1500))
                .currentAmount(BigDecimal.valueOf(1200))
                .user(user)
                .build();

        transaction.setFinancialGoal(financialGoal);

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(exchangeRateService.convertToUSD("USD", BigDecimal.valueOf(1000)))
                .thenReturn(BigDecimal.valueOf(1000));

        // Act
        transactionService.deleteById(transaction.getId());

        // Assert
        assertThat(account.getBalance()).isEqualTo("6000");
        assertThat(financialGoal.getCurrentAmount()).isEqualTo("200");
        verify(transactionRepository).findById(transaction.getId());
        verify(transactionRepository).deleteById(transaction.getId());
        verify(accountService).save(account);
    }
}
