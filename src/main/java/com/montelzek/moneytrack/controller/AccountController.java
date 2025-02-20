package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.UserRepository;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/accounts")
public class AccountController {

    private final UserService userService;
    private final AccountService accountService;

    public AccountController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @GetMapping
    public String listAccount(Model model) {

        Long id = userService.getCurrentUserId();
        Account account = new Account();
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<Account.AccountType> accountTypes = Arrays.asList(Account.AccountType.values());
        List<Account.Currency> currencies = Arrays.asList(Account.Currency.values());

        model.addAttribute("account", account);
        model.addAttribute("accountTypes", accountTypes);
        model.addAttribute("currencies", currencies);
        model.addAttribute("id", id);
        model.addAttribute("accounts", accounts);

        return "accounts/list";
    }

    @PostMapping("/save")
    public String addAccount(@ModelAttribute("account") Account account) {

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        account.setUser(user);

        accountService.save(account);

        return "redirect:/accounts";
    }


}
