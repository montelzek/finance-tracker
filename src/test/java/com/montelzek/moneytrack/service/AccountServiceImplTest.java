package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private UserService userService;

    @InjectMocks
    AccountServiceImpl accountService;

    private Account account;
    private AccountDTO accountDTO;
    private User user;

    @BeforeEach
    public void setup() {
        user = User.builder().id(1L).build();

        account = Account.builder()
                .id(1L)
                .name("Test Account")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(3000))
                .currency(Account.Currency.USD)
                .user(user)
                .build();

        accountDTO = AccountDTO.builder()
                .id(1L)
                .name("Test Account")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(3000))
                .currency(Account.Currency.USD)
                .build();
    }

    @Test
    public void findUsersAccounts_existingUserId_shouldReturnAccountsList() {
        // Arrange
        when(accountRepository.findByUserIdOrderByCreatedAt(user.getId())).thenReturn(List.of(account));

        // Act
        List<Account> result = accountService.findUsersAccounts(user.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUser().getId()).isEqualTo(user.getId());
        verify(accountRepository).findByUserIdOrderByCreatedAt(user.getId());
    }

    @Test
    public void findUsersAccounts_nonExistingUserId_shouldReturnAccountsList() {
        // Arrange
        when(accountRepository.findByUserIdOrderByCreatedAt(-1L)).thenReturn(List.of());

        // Act
        List<Account> result = accountService.findUsersAccounts(-1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(accountRepository).findByUserIdOrderByCreatedAt(-1L);
    }
}
