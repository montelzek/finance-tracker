package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.dto.UserRegisterDTO;
import com.montelzek.moneytrack.model.*;
import com.montelzek.moneytrack.service.*;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ExchangeRateService exchangeRateService;
    private final BudgetService budgetService;
    private final FinancialGoalService financialGoalService;
    private final CategoryService categoryService;

    public AuthController(RoleService roleService,
                          PasswordEncoder passwordEncoder,
                          UserService userService,
                          AccountService accountService,
                          TransactionService transactionService,
                          ExchangeRateService exchangeRateService,
                          BudgetService budgetService,
                          FinancialGoalService financialGoalService,
                          CategoryService categoryService) {
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
        this.exchangeRateService = exchangeRateService;
        this.budgetService = budgetService;
        this.financialGoalService = financialGoalService;
        this.categoryService = categoryService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegisterDTO());
        return "register";
    }

    @PostMapping("/register")
    public String saveUser(@Valid @ModelAttribute("user") UserRegisterDTO userRegisterDTO, BindingResult result) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.existsByEmail(userRegisterDTO.getEmail())) {
            result.rejectValue("email", "error.email", "Email already exists");
            return "register";
        }

        User user = new User(
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword()),
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName()
        );

        Role userRole = roleService.findByName(Role.ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found."));
        user.setRoles(Collections.singleton(userRole));

        userService.save(user);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        LocalDate today = LocalDate.now();

        List<Category> incomeCategories = categoryService.findByType("INCOME");
        List<Category> expenseCategories = categoryService.findByType("EXPENSE");
        List<Category> financialGoalCategories = categoryService.findByType("FINANCIAL_GOAL");
        List<Account> accounts = accountService.findUsersAccounts(userId);
        BigDecimal totalBalance = accountService.getTotalBalance(userId);
        BigDecimal incomeFromLastMonth = transactionService.getIncomeFromLastMonth(userId);
        BigDecimal expenseFromLastMonth = transactionService.getExpensesFromLastMonth(userId);
        Map<String, BigDecimal> expensesByCategory = transactionService.getExpensesByCategoryFromLastMonth(userId);
        Map<String, Map<String, BigDecimal>> transactionsLastSixMonths = transactionService.getTransactionsFromLastSixMonths(userId);
        List<Transaction> recentTransactions = transactionService.getRecentTransactions(userId);
        Map<String, BigDecimal> rates = exchangeRateService.getRates();
        List<Budget> activeBudgets = budgetService.findActiveBudgets(userId, today);
        List<FinancialGoal> financialGoals = financialGoalService.findTop3Goals(userId);


        model.addAttribute("transaction", new TransactionDTO());
        model.addAttribute("incomeCategories", incomeCategories);
        model.addAttribute("expenseCategories", expenseCategories);
        model.addAttribute("financialGoalCategories", financialGoalCategories);
        model.addAttribute("accounts", accounts);
        model.addAttribute("user", user);
        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("incomeFromLastMonth", incomeFromLastMonth);
        model.addAttribute("expensesFromLastMonth", expenseFromLastMonth);
        model.addAttribute("expensesByCategory", expensesByCategory);
        model.addAttribute("transactionsLastSixMonths", transactionsLastSixMonths);
        model.addAttribute("recentTransactions", recentTransactions);
        model.addAttribute("rates", rates);
        model.addAttribute("activeBudgets", activeBudgets);
        model.addAttribute("financialGoals", financialGoals);

        return "dashboard";
    }

    @GetMapping("/access-denied")
    public String showAccessDenied() {
        return "access-denied";
    }
}
