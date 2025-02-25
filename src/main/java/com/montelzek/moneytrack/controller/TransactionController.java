package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.service.CategoryService;
import com.montelzek.moneytrack.service.TransactionService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/transactions")
public class TransactionController {

    private final CategoryService categoryService;
    private final TransactionService transactionService;
    private final UserService userService;

    public TransactionController(CategoryService categoryService, TransactionService transactionService, UserService userService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
        this.userService = userService;
    }

    @GetMapping
    public String listTransactions(Model model) {

        Long id = userService.getCurrentUserId();
        TransactionDTO transactionDTO = new TransactionDTO();
        List<Transaction> transactions = transactionService.findAccountsTransactions(id);
        List<Category> categories = categoryService.findAll();
        model.addAttribute("transactions", transactions);
        model.addAttribute("categories", categories);
        model.addAttribute("transaction", transactionDTO);

        return "transactions/list";
    }

//    @PostMapping("/save")
//    public String saveTransaction(@Valid @ModelAttribute("transaction") TransactionDTO transactionDTO,
//                                  BindingResult result) {
//
//    }
}
