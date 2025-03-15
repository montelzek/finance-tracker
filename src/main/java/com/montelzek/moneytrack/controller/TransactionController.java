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

    public TransactionController(CategoryService categoryService,
                                 TransactionService transactionService,
                                 UserService userService,
                                 AccountService accountService,
                                 FinancialGoalService financialGoalService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.userService = userService;
        this.accountService = accountService;
        this.financialGoalService = financialGoalService;
    }

    @GetMapping
    public String listTransactions(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
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

        try {
            if (transactionDTO.getId() != null) {
                transactionService.updateTransaction(transactionDTO);
            } else {
                transactionService.createTransaction(transactionDTO);
            }
            return "redirect:/transactions";
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Financial Goal ID")) {
                result.rejectValue("financialGoalId", "error.goal", e.getMessage());
            } else if (e.getMessage().contains("already achieved goal")) {
                result.rejectValue("financialGoalId", "error.goal", e.getMessage());
            } else {
                result.rejectValue("", "error.general", e.getMessage());
            }
            prepareTransactionModel(model);
            return "transactions/list";
        }
    }

    @GetMapping("/delete")
    public String deleteTransaction(@RequestParam("transactionId") Long id) {
        transactionService.deleteById(id);
        return "redirect:/transactions";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody
    public TransactionDTO getTransactionForEdit(@PathVariable Long id) {

        Transaction transaction = transactionService.findById(id);
        return transactionService.convertToDTO(transaction);
    }

    private void prepareTransactionModel(Model model) {

        Long id = userService.getCurrentUserId();

        List<Category> categories = categoryService.findAll();
        List<Category> incomeCategories = categoryService.findByType("INCOME");
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        List<Category> financialGoalCategories = categoryService.findByType("FINANCIAL_GOAL");
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<FinancialGoal> financialGoals = financialGoalService.findUsersFinancialGoals(id);

        model.addAttribute("categories", categories);
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("accounts", accounts);
        model.addAttribute("financialGoals", financialGoals);
        model.addAttribute("financialGoalCategories", financialGoalCategories);
    }
}
