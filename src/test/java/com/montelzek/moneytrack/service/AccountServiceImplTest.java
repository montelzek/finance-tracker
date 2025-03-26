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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
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
                .name("Test Account DTO")
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

    @Test
    public void save_validAccount_shouldSaveAccount() {
        // Arrange
        when(accountRepository.save(account)).thenReturn(account);

        // Act
        Account savedAccount = accountService.save(account);

        // Assert
        assertThat(savedAccount).isEqualTo(account);
        verify(accountRepository).save(account);
    }

    @Test
    public void findById_existingId_shouldReturnAccount() {
        // Arrange
        Long accountId = account.getId();
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // Act
        Account foundAccount = accountService.findById(accountId);

        // Assert
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getName()).isEqualTo(account.getName());
        assertThat(foundAccount.getAccountType()).isEqualTo(account.getAccountType());
        assertThat(foundAccount.getCurrency()).isEqualTo(account.getCurrency());
        assertThat(foundAccount.getBalance()).isEqualTo(account.getBalance());
        verify(accountRepository).findById(accountId);
    }

    @Test
    public void findById_nonExistingId_shouldThrowRuntimeException() {
        // Arrange
        Long nonExistingId = -1L;
        when(accountRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> accountService.findById(nonExistingId))
                .withMessage("Did not find account of id: " + nonExistingId);
        verify(accountRepository).findById(nonExistingId);
    }

    @Test
    public void deleteById_shouldCallRepositoryDelete() {
        // Arrange
        Long testId = 1L;
        // Act
        accountService.deleteById(testId);
        //Assert
        verify(accountRepository).deleteById(testId);
    }

    @Test
    public void saveAccount_idIsNull_shouldCreateNewAccount() {
        // Arrange
        AccountDTO newAccountDTO = AccountDTO.builder()
                .id(null)
                .name("New Account")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(3000))
                .currency(Account.Currency.USD)
                .build();

        when(userService.getCurrentUserId()).thenReturn(1L);
        when(userService.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        // Act
        accountService.saveAccount(newAccountDTO);

        // Assert
        verify(userService).getCurrentUserId();
        verify(userService).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void saveAccount_idIsNotNull_shouldUpdateAccount() {
        // Arrange
        AccountDTO existingAccountDTO = AccountDTO.builder()
                .id(1L)
                .name("Update Account")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(3000))
                .currency(Account.Currency.USD)
                .build();

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        // Act
        accountService.saveAccount(existingAccountDTO);

        // Assert
        assertThat(account.getName()).isEqualTo("Update Account");
        verify(accountRepository).findById(1L);
        verify(accountRepository).save(account);
    }
}
