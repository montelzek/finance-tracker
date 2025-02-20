package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.UserRepository;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        AccountDTO accountDTO = new AccountDTO();
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<Account.AccountType> accountTypes = Arrays.asList(Account.AccountType.values());
        List<Account.Currency> currencies = Arrays.asList(Account.Currency.values());

        model.addAttribute("account", accountDTO);
        model.addAttribute("accountTypes", accountTypes);
        model.addAttribute("currencies", currencies);
        model.addAttribute("accounts", accounts);

        return "accounts/list";
    }

    // Endpoint to fetch account data for editing

    @GetMapping("/edit/{id}")
    @ResponseBody
    public AccountDTO getAccountForEdit(@PathVariable Long id) {

        Account account = accountService.findById(id);

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setName(account.getName());
        accountDTO.setAccountType(account.getAccountType());
        accountDTO.setBalance(account.getBalance());
        accountDTO.setCurrency(account.getCurrency());
        return accountDTO;
    }

    @PostMapping("/save")
    public String saveAccount(@ModelAttribute("account") AccountDTO accountDTO) {

        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Account account;
        if (accountDTO.getId() != null) {
            account = accountService.findById(accountDTO.getId());
            account.setName(accountDTO.getName());
            account.setAccountType(accountDTO.getAccountType());
            account.setBalance(accountDTO.getBalance());
            account.setCurrency(accountDTO.getCurrency());
        } else {
            account = new Account(
                    accountDTO.getName(),
                    accountDTO.getAccountType(),
                    accountDTO.getBalance(),
                    accountDTO.getCurrency()
            );
            account.setUser(user);
        }


        accountService.save(account);
        return "redirect:/accounts";
    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("accountId") Long id) {
        accountService.deleteById(id);
        return "redirect:/accounts";
    }

}
