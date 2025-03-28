package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Transaction;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    public void setup() {

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
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> expectedPage = Page.empty();

        when(transactionRepository.findByAccount_User_Id_OrderByDateDescCreatedAtDesc(anyLong(), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<Transaction> result = transactionService.findAccountsTransactions(userId, pageable);

        // Assert
        assertThat(result).isEmpty();
        verify(transactionRepository).findByAccount_User_Id_OrderByDateDescCreatedAtDesc(userId, pageable);
    }
}
