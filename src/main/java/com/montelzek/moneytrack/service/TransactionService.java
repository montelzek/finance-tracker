package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.*;
import com.montelzek.moneytrack.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final FinancialGoalService financialGoalService;
    private final UserService userService;

    public TransactionService(TransactionRepository transactionRepository,
                              BudgetService budgetService,
                              ExchangeRateService exchangeRateService,
                              AccountService accountService,
                              CategoryService categoryService,
                              FinancialGoalService financialGoalService, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.budgetService = budgetService;
        this.exchangeRateService = exchangeRateService;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.financialGoalService = financialGoalService;
        this.userService = userService;
    }

    public Page<Transaction> findAccountsTransactions(Long id, Pageable pageable) {
        return transactionRepository.findByAccount_User_Id_OrderByDateDescCreatedAtDesc(id, pageable);
    }

    public List<Transaction> getRecentTransactions(Long id) {
        return transactionRepository.findTop6ByAccount_User_Id_OrderByDateDesc(id);
    }

    public Transaction findById(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
    }

    @Transactional
    public Transaction save(Transaction transaction) {
        applyBudgetEffect(transaction);
        return transactionRepository.save(transaction);
    }

    @Transactional
    public void deleteById(Long id) {
        Long userId = userService.getCurrentUserId();
        Transaction transaction = findById(id);
        Account account = transaction.getAccount();
        revertBalanceEffect(transaction, account);
        revertBudgetEffect(transaction);
        transactionRepository.deleteById(id);

        accountService.saveAccount(account, userId);
    }

    @Transactional
    public void createTransaction(TransactionDTO transactionDTO) {
        Account account = accountService.findById(transactionDTO.getAccountId());
        Category category = categoryService.findById(transactionDTO.getCategoryId());

        Transaction transaction = new Transaction(
                transactionDTO.getAmount(),
                transactionDTO.getDate(),
                transactionDTO.getDescription()
        );
        transaction.setAccount(account);
        transaction.setCategory(category);

        processTransactionByType(transaction, transactionDTO, account, category, null);
        applyBudgetEffect(transaction);
        transactionRepository.save(transaction);
    }

    @Transactional
    public void updateTransaction(TransactionDTO transactionDTO) {
        if (transactionDTO.getId() == null) {
            throw new IllegalArgumentException("Transaction ID can't be null");
        }

        Transaction existingTransaction = findById(transactionDTO.getId());
        Account existingAccount = existingTransaction.getAccount();
        Account newAccount = accountService.findById(transactionDTO.getAccountId());
        Category newCategory = categoryService.findById(transactionDTO.getCategoryId());

        revertTransactionEffect(existingTransaction, existingAccount, transactionDTO);

        existingTransaction.setAccount(newAccount);
        existingTransaction.setDate(transactionDTO.getDate());
        existingTransaction.setAmount(transactionDTO.getAmount());
        existingTransaction.setCategory(newCategory);
        existingTransaction.setDescription(transactionDTO.getDescription());

        processTransactionByType(existingTransaction, transactionDTO, newAccount, newCategory, existingAccount);
        applyBudgetEffect(existingTransaction);
        transactionRepository.save(existingTransaction);
    }

    private void revertTransactionEffect(Transaction transaction, Account account, TransactionDTO transactionDTO) {
        switch (transaction.getCategory().getType()) {
            case "INCOME" ->
                    account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            case "EXPENSE" -> {
                revertBudgetEffect(transaction);
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }
            case "FINANCIAL_GOAL" -> {
                Long financialGoalId = (transactionDTO.getFinancialGoalId() != null)
                        ? transactionDTO.getFinancialGoalId()
                        : (transaction.getFinancialGoal() != null ? transaction.getFinancialGoal().getId() : null);
                if (financialGoalId != null) {
                    FinancialGoal financialGoal = financialGoalService.findById(financialGoalId);
                    updateFinancialGoal(transaction, account, financialGoal);
                }
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }
        }
    }

    private void processTransactionByType(Transaction transaction, TransactionDTO transactionDTO,
                                          Account account, Category category, Account oldAccount) {
        switch (category.getType()) {
            case "INCOME" -> account.setBalance(account.getBalance().add(transaction.getAmount()));
            case "EXPENSE" -> account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            case "FINANCIAL_GOAL" -> {
                Long financialGoalId = transactionDTO.getFinancialGoalId();
                if (financialGoalId == null) {
                    throw new IllegalArgumentException("Financial Goal ID is required for FINANCIAL_GOAL transaction type");
                }
                FinancialGoal financialGoal = financialGoalService.findById(financialGoalId);
                if (financialGoal.getIsAchieved()) {
                    throw new IllegalArgumentException("Can't add transaction to already achieved goal");
                }
                transaction.setFinancialGoal(financialGoal);
                String currency = account.getCurrency().toString();
                BigDecimal amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());
                financialGoal.setCurrentAmount(financialGoal.getCurrentAmount().add(amountInUSD));
                financialGoal.setIsAchieved(financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) >= 0);
                financialGoalService.save(financialGoal);
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            }
        }

        accountService.saveAccount(account, userService.getCurrentUserId());

        // If old account is different from new account and exists, save it too
        if (oldAccount != null && !oldAccount.getId().equals(account.getId())) {
            accountService.saveAccount(oldAccount, userService.getCurrentUserId());
        }
    }

    private void revertBalanceEffect(Transaction transaction, Account account) {
        switch (transaction.getCategory().getType()) {
            case "INCOME" -> account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            case "EXPENSE" -> account.setBalance(account.getBalance().add(transaction.getAmount()));
            case "FINANCIAL_GOAL" -> {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
                FinancialGoal financialGoal = transaction.getFinancialGoal();
                if (financialGoal != null) {
                    updateFinancialGoal(transaction, account, financialGoal);
                }
            }
        }
    }

    private void updateFinancialGoal(Transaction transaction, Account account, FinancialGoal financialGoal) {
        String currency = account.getCurrency().toString();
        BigDecimal amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());
        financialGoal.setCurrentAmount(financialGoal.getCurrentAmount().subtract(amountInUSD));
        financialGoal.setIsAchieved(financialGoal.getCurrentAmount().compareTo(financialGoal.getTargetAmount()) >= 0);
        financialGoalService.save(financialGoal);
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
                transactionsGroupByMonth.get(monthKey).computeIfPresent(type, (_, current) -> current.add(amount));
            }
        }

        return transactionsGroupByMonth;
    }

    public TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId(transaction.getId());
        transactionDTO.setAmount(transaction.getAmount());
        transactionDTO.setDate(transaction.getDate());
        transactionDTO.setDescription(transaction.getDescription());
        transactionDTO.setAccountId(transaction.getAccount().getId());
        transactionDTO.setCategoryId(transaction.getCategory().getId());
        transactionDTO.setCategoryType(transaction.getCategory().getType());
        if (transaction.getFinancialGoal() != null) {
            transactionDTO.setFinancialGoalId(transaction.getFinancialGoal().getId());
        }
        return transactionDTO;
    }
}
