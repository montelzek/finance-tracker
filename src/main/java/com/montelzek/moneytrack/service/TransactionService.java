package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.repository.TransactionRepository;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.ResourceUrlEncodingFilter;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final BudgetService budgetService;
    private final ExchangeRateService exchangeRateService;

    public TransactionService(TransactionRepository transactionRepository, BudgetService budgetService, ExchangeRateService exchangeRateService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
        this.exchangeRateService = exchangeRateService;
    }

    public List<Transaction> findAccountsTransactions(Long id) {
        return transactionRepository.findByAccount_User_Id_OrderByCreatedAt(id);
    }

    public void save(Transaction transaction) {
        transactionRepository.save(transaction);

        if (transaction.getCategory().getType().equals("EXPENSE")) {
            List<Budget> budgets = budgetService.findBudgetsByCategoryAndDate(
                    transaction.getCategory(),
                    transaction.getDate()
            );
            if (!budgets.isEmpty()) {
                Budget budget = budgets.getFirst();
                String currency = String.valueOf(transaction.getAccount().getCurrency());
                Double amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());
                budget.setBudgetSpent(budget.getBudgetSpent() + amountInUSD);
                budgetService.save(budget);
            }
        }
    }

    public void deleteById(Long id) {
        transactionRepository.deleteById(id);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public List<Transaction> findIncomeTransactionsFromPastMonth(Long userId) {
        return transactionRepository.findIncomeTransactionsFromPastMonth(userId);
    }

    public Double getIncomeFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findIncomeTransactionsFromPastMonth(userId);
        Double totalIncome = 0.0;

        for (Transaction transaction : transactions) {
            totalIncome += exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());
        }

        return totalIncome;
    }

    public Double getExpensesFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findExpenseTransactionsFromPastMonth(userId);
        Double totalExpenses = 0.0;

        for (Transaction transaction : transactions) {
            totalExpenses += exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());
        }

        return totalExpenses;
    }
}
