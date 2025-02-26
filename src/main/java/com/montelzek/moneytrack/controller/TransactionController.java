package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.CategoryService;
import com.montelzek.moneytrack.service.TransactionService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
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

    public TransactionController(CategoryService categoryService, TransactionService transactionService, UserService userService, AccountService accountService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public String listTransactions(Model model) {

        TransactionDTO transactionDTO = new TransactionDTO();
        model.addAttribute("transaction", transactionDTO);
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

        transactionService.save(transaction);

        return "redirect:/transactions";
    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("transactionId") Long id) {

        Transaction transaction = transactionService.findById(id);
        Account account = transaction.getAccount();

        if (transaction.getCategory().getType().equals("INCOME")) {
            account.setBalance(account.getBalance() - transaction.getAmount());
        } else {
            account.setBalance(account.getBalance() + transaction.getAmount());
        }

        accountService.save(account);

        transactionService.deleteById(id);
        return "redirect:/transactions";
    }

    private void prepareTransactionModel(Model model) {
        Long id = userService.getCurrentUserId();
        List<Transaction> transactions = transactionService.findAccountsTransactions(id);
        List<Category> categories = categoryService.findAll();
        List<Category> incomeCategories = categoryService.findByType("INCOME");
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        List<Account> accounts = accountService.findUsersAccounts(id);
        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categories);
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("accounts", accounts);
    }
}
