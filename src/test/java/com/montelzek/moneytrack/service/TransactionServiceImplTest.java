package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.model.User;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();

        account = Account.builder()
                .id(1L)
                .user(user)
                .build();

        transaction = Transaction.builder()
                .id(1L)
                .account(account)
                .amount(BigDecimal.valueOf(1000))
                .date(LocalDate.now())
                .build();
    }

    @Test
    public void findAccountsTransactions_transactionsExists_shouldReturnPageOfTransactions() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Transaction> transactions = List.of(new Transaction(), new Transaction());
        Page<Transaction> expectedPage = new PageImpl<>(transactions, pageable, transactions.size());

        when(transactionRepository.findByAccount_User_Id_OrderByDateDescCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<Transaction> result = transactionService.findAccountsTransactions(userId, pageable);

        // Assert
        assertThat(result).isEqualTo(expectedPage);
        assertThat(result.getContent()).hasSize(2);
        verify(transactionRepository).findByAccount_User_Id_OrderByDateDescCreatedAtDesc(userId, pageable);
    }

    @Test
    public void findAccountsTransactions_transactionsNotExists_shouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> expectedPage = Page.empty();

        when(transactionRepository.findByAccount_User_Id_OrderByDateDescCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<Transaction> result = transactionService.findAccountsTransactions(user.getId(), pageable);

        // Assert
        assertThat(result).isEmpty();
        verify(transactionRepository).findByAccount_User_Id_OrderByDateDescCreatedAtDesc(user.getId(), pageable);
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
}
