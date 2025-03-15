package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.AccountDTO;
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

    public Account save(Account account) {
        return accountRepository.save(account);
    }

    public Account findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Did not find account of id: " + id));
    }

    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Transactional
    public void saveAccount(AccountDTO accountDTO) {
        if (accountDTO.getId() != null) {
            updateAccount(accountDTO);
        } else {
            createAccount(accountDTO);
        }
    }

    @Transactional
    public void createAccount(AccountDTO accountDTO) {
        Long userId = userService.getCurrentUserId();
        User user = userService.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Account account = new Account(
                accountDTO.getName(),
                accountDTO.getAccountType(),
                accountDTO.getBalance(),
                accountDTO.getCurrency()
        );
        account.setUser(user);

        save(account);
    }

    @Transactional
    public void updateAccount(AccountDTO accountDTO) {
        if (accountDTO.getId() == null) {
            throw new IllegalArgumentException("Budget ID can't be null");
        }

        Account account = findById(accountDTO.getId());

        account.setName(accountDTO.getName());
        account.setAccountType(accountDTO.getAccountType());
        account.setBalance(accountDTO.getBalance());
        account.setCurrency(accountDTO.getCurrency());

        save(account);
    }

    public BigDecimal getTotalBalance(Long userId) {

        List<Account> accounts = accountRepository.findByUserId(userId);
        BigDecimal totalBalance = BigDecimal.ZERO;

        for (Account account : accounts) {
            totalBalance = totalBalance.add(exchangeRateService.convertToUSD(String.valueOf(account.getCurrency()), account.getBalance()));
        }

        return totalBalance;
    }

    public AccountDTO convertToDTO(Account account) {

        AccountDTO accountDTO = new AccountDTO();
        accountDTO.setId(account.getId());
        accountDTO.setName(account.getName());
        accountDTO.setAccountType(account.getAccountType());
        accountDTO.setBalance(account.getBalance());
        accountDTO.setCurrency(account.getCurrency());
        return accountDTO;
    }

}
