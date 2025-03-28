package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface TransactionService {

    Page<Transaction> findAccountsTransactions(Long id, Pageable pageable);

    List<Transaction> getRecentTransactions(Long id);

    Transaction findById(Long id);

    void deleteById(Long id);

    void createTransaction(TransactionDTO transactionDTO);

    void updateTransaction(TransactionDTO transactionDTO);

    BigDecimal getIncomeFromLastMonth(Long userId);

    BigDecimal getExpensesFromLastMonth(Long userId);

    Map<String, BigDecimal> getExpensesByCategoryFromLastMonth(Long userId);

    Map<String, Map<String, BigDecimal>> getTransactionsFromLastSixMonths(Long userId);

    TransactionDTO convertToDTO(Transaction transaction);
}
