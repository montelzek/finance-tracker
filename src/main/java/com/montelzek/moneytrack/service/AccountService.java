package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    List<Account> findUsersAccounts(Long id);

    Account save(Account account);

    Account findById(Long id);

    void deleteById(Long id);

    void saveAccount(AccountDTO accountDTO);

    void createAccount(AccountDTO accountDTO);

    void updateAccount(AccountDTO accountDTO);

    BigDecimal getTotalBalance(Long userId);

    AccountDTO convertToDTO(Account account);
}