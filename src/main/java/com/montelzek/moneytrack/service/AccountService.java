package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;

    public AccountService(AccountRepository accountRepository, ExchangeRateService exchangeRateService) {
        this.accountRepository = accountRepository;
        this.exchangeRateService = exchangeRateService;
    }

    public List<Account> findUsersAccounts(Long id) {
        return accountRepository.findByUserIdOrderByCreatedAt(id);
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public Account findById(Long id) {

        Optional<Account> result = accountRepository.findById(id);
        Account account = null;

        if (result.isPresent()) {
            account = result.get();
        } else {
            throw new RuntimeException("Did not find account of id: " + id);
        }

        return account;
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    public Double getTotalBalance(Long userId) {

        List<Account> accounts = accountRepository.findByUserId(userId);
        Double totalBalance = 0.0;

        for (Account account : accounts) {
            totalBalance += exchangeRateService.convertToUSD(String.valueOf(account.getCurrency()), account.getBalance());
        }

        return totalBalance;
    }

}
