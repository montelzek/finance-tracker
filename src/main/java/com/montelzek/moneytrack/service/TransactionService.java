package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Page<Transaction> findAccountsTransactions(Long id, Pageable pageable) {
        return transactionRepository.findByAccount_User_Id_OrderByDateDescCreatedAtDesc(id, pageable);
    }

    public List<Transaction> getRecentTransactions(Long id) {
        return transactionRepository.findTop6ByAccount_User_Id_OrderByDateDesc(id);
    }

    public void save(Transaction transaction) {

        applyBudgetEffect(transaction);
        transactionRepository.save(transaction);
    }

    public void deleteById(Long id) {
        Transaction transaction = findById(id);
        revertBudgetEffect(transaction);
        transactionRepository.deleteById(id);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    public BigDecimal getIncomeFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findIncomeTransactionsFromPastMonth(userId);
        BigDecimal totalIncome = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            totalIncome = totalIncome.add(exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount()));
        }

        return totalIncome;
    }

    public BigDecimal getExpensesFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findExpenseTransactionsFromPastMonth(userId);
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction transaction : transactions) {
            totalExpenses = totalExpenses.add(exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount()));
        }

        return totalExpenses;
    }

    public Map<String, BigDecimal> getExpensesByCategoryFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findExpenseTransactionsFromPastMonth(userId);
        return getStringBigDecimalMap(transactions);
    }

    private Map<String, BigDecimal> getStringBigDecimalMap(List<Transaction> transactions) {
        Map<String, BigDecimal> transactionsByCategory = new HashMap<>();

        for (Transaction transaction : transactions) {

            String categoryName = transaction.getCategory().getName();
            BigDecimal amount = exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());

            amount = amount.setScale(2, RoundingMode.HALF_UP);
            transactionsByCategory.merge(categoryName, amount, BigDecimal::add);
        }

        return transactionsByCategory;
    }


    public Map<String, Map<String, BigDecimal>> getTransactionsFromLastSixMonths(Long userId) {

        LocalDate sixMonthAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Transaction> transactions = transactionRepository.findTransactionsFromLastSixMonths(userId, sixMonthAgo);

        Map<String, Map<String, BigDecimal>> transactionsGroupByMonth = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthKey = month.format(formatter);
            transactionsGroupByMonth.put(monthKey, new HashMap<>());
            transactionsGroupByMonth.get(monthKey).put("INCOME", BigDecimal.ZERO);
            transactionsGroupByMonth.get(monthKey).put("EXPENSE", BigDecimal.ZERO);
        }

        for (Transaction transaction : transactions) {
            String monthKey = transaction.getDate().withDayOfMonth(1).format(formatter);
            String type = transaction.getCategory().getType();
            BigDecimal amount = exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());

            if (transactionsGroupByMonth.containsKey(monthKey)) {
                BigDecimal current = transactionsGroupByMonth.get(monthKey).get(type);
                transactionsGroupByMonth.get(monthKey).put(type, current.add(amount));
            }
        }

        return transactionsGroupByMonth;
    }

    public void revertBudgetEffect(Transaction transaction) {
        if (transaction.getBudget() != null && "EXPENSE".equals(transaction.getCategory().getType())) {
            Budget budget = transaction.getBudget();
            String currency = String.valueOf(transaction.getAccount().getCurrency());
            BigDecimal amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());
            budget.setBudgetSpent(budget.getBudgetSpent().subtract(amountInUSD));
            budgetService.save(budget);
            transaction.setBudget(null);
        }
    }

    public void applyBudgetEffect(Transaction transaction) {
        if ("EXPENSE".equals(transaction.getCategory().getType())) {
            List<Budget> budgets = budgetService.findBudgetsByCategoryAndDate(
                    transaction.getAccount().getUser().getId(),
                    transaction.getCategory(),
                    transaction.getDate()
            );
            if (!budgets.isEmpty()) {
                Budget budget = budgets.getFirst();
                transaction.setBudget(budget);
                String currency = String.valueOf(transaction.getAccount().getCurrency());
                BigDecimal amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());
                budget.setBudgetSpent(budget.getBudgetSpent().add(amountInUSD));
                budgetService.save(budget);
            } else {
                transaction.setBudget(null);
            }
        } else {
            transaction.setBudget(null);
        }
    }
}
