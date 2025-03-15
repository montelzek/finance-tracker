package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;
    private final UserService userService;

    public AccountService(AccountRepository accountRepository, ExchangeRateService exchangeRateService, UserService userService) {
        this.accountRepository = accountRepository;
        this.exchangeRateService = exchangeRateService;
        this.userService = userService;
    }

    public List<Account> findUsersAccounts(Long id) {
        return accountRepository.findByUserIdOrderByCreatedAt(id);
    }

    @Transactional
    public void saveAccount(Account account, Long userId) {

        if (account.getId() == null) {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            account.setUser(user);
        }
        accountRepository.save(account);
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Did not find account of id: " + id));
    }

    public void deleteAccount(Long id) {
        accountRepository.deleteById(id);
    }

    public BigDecimal getTotalBalance(Long userId) {

        List<Account> accounts = accountRepository.findByUserId(userId);
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : accounts) {
            totalBalance = totalBalance.add(exchangeRateService.convertToUSD(String.valueOf(account.getCurrency()), account.getBalance()));
        }

        return totalBalance;
    }

}
