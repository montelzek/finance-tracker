package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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

        AccountDTO accountDTO = new AccountDTO();
        model.addAttribute("account", accountDTO);
        prepareAccountModel(model);

        return "accounts/list";
    }

    // Endpoint to fetch account data for editing

    @GetMapping("/edit/{id}")
    @ResponseBody
    public AccountDTO getAccountForEdit(@PathVariable Long id) {

        Account account = accountService.findById(id);
        return accountService.convertToDTO(account);
    }

    @PostMapping("/save")
    public String saveAccount(@Valid @ModelAttribute("account") AccountDTO accountDTO,
                              BindingResult result, Model model) {

        if (result.hasErrors()) {
            model.addAttribute("account", accountDTO);
            prepareAccountModel(model);
            return "accounts/list";
        }

        try {
            accountService.saveAccount(accountDTO);
            return "redirect:/accounts";
        } catch (IllegalArgumentException | IllegalStateException e) {
            result.rejectValue("", "error.general", e.getMessage());
            prepareAccountModel(model);
            return "accounts/list";
        }


    }

    @GetMapping("/delete")
    public String deleteAccount(@RequestParam("accountId") Long id) {
        accountService.deleteById(id);
        return "redirect:/accounts";
    }


    private void prepareAccountModel(Model model) {
        Long id = userService.getCurrentUserId();
        List<Account> accounts = accountService.findUsersAccounts(id);
        List<Account.AccountType> accountTypes = Arrays.asList(Account.AccountType.values());
        List<Account.Currency> currencies = Arrays.asList(Account.Currency.values());
        model.addAttribute("accountTypes", accountTypes);
        model.addAttribute("currencies", currencies);
        model.addAttribute("accounts", accounts);
    }

}
