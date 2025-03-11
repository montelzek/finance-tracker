package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.FinancialGoal;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final UserService userService;
    private final AccountService accountService;
    private final FinancialGoalService financialGoalService;
    private final ExchangeRateService exchangeRateService;

    public TransactionController(CategoryService categoryService, TransactionService transactionService, UserService userService, AccountService accountService, FinancialGoalService financialGoalService, ExchangeRateService exchangeRateService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
        this.financialGoalService = financialGoalService;
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
                                   Model model) {

        Long id = userService.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactionPage = transactionService.findAccountsTransactions(id, pageable);

        TransactionDTO transactionDTO = new TransactionDTO();
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("transactionsPage", transactionPage);
        prepareTransactionModel(model);

        return "transactions/list";
    }

    @PostMapping("/save")
    public String saveTransaction(@Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                  BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("transaction", transactionDTO);
            prepareTransactionModel(model);
            return "transactions/list";
        }

        Account account = accountService.findById(transactionDTO.getAccountId());
        Category category = categoryService.findById(transactionDTO.getCategoryId());

        if (transactionDTO.getId() != null) {
            Transaction existingTransaction = transactionService.findById(transactionDTO.getId()); // TRANSACTION before update
            Account existingAccount = existingTransaction.getAccount(); // OLD ACCOUNT

            // Reversing balance

            if (existingTransaction.getCategory().getType().equals("INCOME")) {
                existingAccount.setBalance(existingAccount.getBalance() - existingTransaction.getAmount());
            } else {
                existingAccount.setBalance(existingAccount.getBalance() + existingTransaction.getAmount());
            }

            // If account is changing then save the old account balance

            if (!existingAccount.getId().equals(account.getId())) {
                accountService.save(existingAccount);
            }

            // Updating transaction

            existingTransaction.setAccount(account);
            existingTransaction.setDate(transactionDTO.getDate());
            existingTransaction.setAmount(transactionDTO.getAmount());
            existingTransaction.setCategory(category);
            existingTransaction.setDescription(transactionDTO.getDescription());

            // Updating balance

            if (category.getType().equals("INCOME")) {
                account.setBalance(account.getBalance() + existingTransaction.getAmount());
            } else {
                account.setBalance(account.getBalance() - existingTransaction.getAmount());
            }

            transactionService.save(existingTransaction);

        } else {
            Transaction transaction = new Transaction(
                    transactionDTO.getAmount(),
                    transactionDTO.getDate(),
                    transactionDTO.getDescription()
            );
            transaction.setAccount(account);
            transaction.setCategory(category);

            if (category.getType().equals("INCOME")) {
                account.setBalance(account.getBalance() + transaction.getAmount());
            } else {
                account.setBalance(account.getBalance() - transaction.getAmount());
            }

            if (category.getType().equals("FINANCIAL_GOAL")) {

                Long financialGoalId = transactionDTO.getFinancialGoalId();
                FinancialGoal financialGoal = financialGoalService.findById(financialGoalId);

                if (financialGoal.getIsAchieved()) {
                    result.rejectValue("financialGoalId", "error.goal", "Can't add transaction to already achieved goal!");
                    prepareTransactionModel(model);
                    return "transactions/list";
                }

                String currency = account.getCurrency().toString();
                Double amountInUSD = exchangeRateService.convertToUSD(currency, transaction.getAmount());

                financialGoal.setCurrentAmount(financialGoal.getCurrentAmount() + amountInUSD);

                if (financialGoal.getCurrentAmount() >= financialGoal.getTargetAmount()) {
                    financialGoal.setIsAchieved(true);
                }

                financialGoalService.save(financialGoal);
            }

            transactionService.save(transaction);
        }

        return "redirect:/transactions";
    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("transactionId") Long id) {

        Transaction transaction = transactionService.findById(id);
        Account account = transaction.getAccount();

        // Reversing balance

        if (transaction.getCategory().getType().equals("INCOME")) {
            account.setBalance(account.getBalance() - transaction.getAmount());
        } else {
            account.setBalance(account.getBalance() + transaction.getAmount());
        }

        accountService.save(account);

        transactionService.deleteById(id);
        return "redirect:/transactions";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public TransactionDTO getTransactionForEdit(@PathVariable Long id) {

        Transaction transaction = transactionService.findById(id);

        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setId(transaction.getId());
        transactionDTO.setAmount(transaction.getAmount());
        transactionDTO.setDate(transaction.getDate());
        transactionDTO.setDescription(transaction.getDescription());
        transactionDTO.setAccountId(transaction.getAccount().getId());
        transactionDTO.setCategoryId(transaction.getCategory().getId());
        transactionDTO.setCategoryType(transaction.getCategory().getType());
        return transactionDTO;
    }

    private void prepareTransactionModel(Model model) {
        Long id = userService.getCurrentUserId();
//        List<Transaction> transactions = transactionService.findAccountsTransactions(id);
        List<Category> categories = categoryService.findAll();
        List<Category> incomeCategories = categoryService.findByType("INCOME");
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        List<Category> financialGoalCategories = categoryService.findByType("FINANCIAL_GOAL");
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<FinancialGoal> financialGoals = financialGoalService.findUsersFinancialGoals(id);
//        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categories);
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("accounts", accounts);
        model.addAttribute("financialGoals", financialGoals);
        model.addAttribute("financialGoalCategories", financialGoalCategories);
    }
}
