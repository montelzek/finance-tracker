package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Budget;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public Map<String, Double> getExpensesByCategoryFromLastMonth(Long userId) {

        List<Transaction> transactions = transactionRepository.findExpenseTransactionsFromPastMonth(userId);
        return getStringDoubleMap(transactions);
    }

    private Map<String, Double> getStringDoubleMap(List<Transaction> transactions) {
        Map<String, Double> transactionsByCategory = new HashMap<>();

        for (Transaction transaction : transactions) {

            String categoryName = transaction.getCategory().getName();
            Double amount = exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());

            amount = Math.round(amount * 100.0) / 100.0;
            transactionsByCategory.merge(categoryName, amount, Double::sum);
        }

        return transactionsByCategory;
    }


    public Map<String, Map<String, Double>> getTransactionsFromLastSixMonths(Long userId) {

        LocalDate sixMonthAgo = LocalDate.now().minusMonths(6).withDayOfMonth(1);
        List<Transaction> transactions = transactionRepository.findTransactionsFromLastSixMonths(userId, sixMonthAgo);

        Map<String, Map<String, Double>> transactionsGroupByMonth = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            String monthKey = month.format(formatter);
            transactionsGroupByMonth.put(monthKey, new HashMap<>());
            transactionsGroupByMonth.get(monthKey).put("INCOME", 0.0);
            transactionsGroupByMonth.get(monthKey).put("EXPENSE", 0.0);
        }

        for (Transaction transaction : transactions) {
            String monthKey = transaction.getDate().withDayOfMonth(1).format(formatter);
            String type = transaction.getCategory().getType();
            Double amount = exchangeRateService.convertToUSD(
                    String.valueOf(transaction.getAccount().getCurrency()), transaction.getAmount());

            if (transactionsGroupByMonth.containsKey(monthKey)) {
                Double current = transactionsGroupByMonth.get(monthKey).get(type);
                transactionsGroupByMonth.get(monthKey).put(type, current + amount);
            }
        }

        return transactionsGroupByMonth;
    }


}
