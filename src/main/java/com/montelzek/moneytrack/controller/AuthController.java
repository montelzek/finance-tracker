package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.UserRegisterDTO;
import com.montelzek.moneytrack.model.Role;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.RoleRepository;
import com.montelzek.moneytrack.repository.UserRepository;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.TransactionService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, UserService userService, AccountService accountService, TransactionService transactionService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping("/login")
    public String login() {
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

        if (userRepository.existsByEmail(userRegisterDTO.getEmail())) {
            result.rejectValue("email", "error.email", "Email already exists");
            return "register";
        }

        User user = new User(
                userRegisterDTO.getEmail(),
                passwordEncoder.encode(userRegisterDTO.getPassword()),
                userRegisterDTO.getFirstName(),
                userRegisterDTO.getLastName()
        );

        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found."));
        user.setRoles(Collections.singleton(userRole));

        userRepository.save(user);

        return "redirect:/login?registered=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        model.addAttribute("user", user);
        model.addAttribute("totalBalance", accountService.getTotalBalance(userId));
        model.addAttribute("incomeFromLastMonth", transactionService.getIncomeFromLastMonth(userId));
        model.addAttribute("expensesFromLastMonth", transactionService.getExpensesFromLastMonth(userId));
        model.addAttribute("expensesByCategory", transactionService.getExpensesByCategoryFromLastMonth(userId));
        model.addAttribute("transactionsLastSixMonths", transactionService.getTransactionsFromLastSixMonths(userId));

        return "dashboard";
    }
}
