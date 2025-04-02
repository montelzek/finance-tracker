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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
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
                                   @RequestParam(required = false, defaultValue = "date") String sortField,
                                   @RequestParam(required = false, defaultValue = "desc") String sortDir,
                                   @RequestParam(required = false) Long filterAccountId,
                                   @RequestParam(required = false) Long filterCategoryId,
                                   @RequestParam(required = false) String filterType,
                                   @RequestParam(required = false) LocalDate filterStartDate,
                                   @RequestParam(required = false) LocalDate filterEndDate,
                                   Model model) {

        Long userId = userService.getCurrentUserId();
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ?
                Sort.by(sortField).ascending() : Sort.by(sortField).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Transaction> transactionPage = transactionService.findTransactions(userId, filterAccountId,
                filterCategoryId, filterType, filterStartDate, filterEndDate, pageable);

        TransactionDTO transactionDTO = new TransactionDTO();
        model.addAttribute("transaction", transactionDTO);
        model.addAttribute("transactionsPage", transactionPage);

        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("filterAccountId", filterAccountId);
        model.addAttribute("filterCategoryId", filterCategoryId);
        model.addAttribute("filterType", filterType);
        model.addAttribute("filterStartDate", filterStartDate);
        model.addAttribute("filterEndDate", filterEndDate);

        prepareTransactionModel(model);

        return "transactions/list";
    }

    @PostMapping("/save")
    public String saveTransaction(@Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  @RequestParam(value = "source", defaultValue = "transactions") String source) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transaction", result);
            redirectAttributes.addFlashAttribute("transaction", transactionDTO);
            return "redirect:/" + source;
        }

        try {
            if (transactionDTO.getId() != null) {
                transactionService.updateTransaction(transactionDTO);
            } else {
                transactionService.createTransaction(transactionDTO);
            }
            return "redirect:/" + source;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/" + source;
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

        List<Category> incomeCategories = categoryService.findByType("INCOME");
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        List<Category> financialGoalCategories = categoryService.findByType("FINANCIAL_GOAL");
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<FinancialGoal> financialGoals = financialGoalService.findUsersFinancialGoals(id);
        List<Category> allCategories = categoryService.findAll();

        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("accounts", accounts);
        model.addAttribute("financialGoals", financialGoals);
        model.addAttribute("financialGoalCategories", financialGoalCategories);
        model.addAttribute("allCategories", allCategories);
    }
}
