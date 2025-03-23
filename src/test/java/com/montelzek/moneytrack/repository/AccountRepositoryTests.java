package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class AccountRepositoryTests {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User testUser;
    private Account account1;
    private Account account2;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("testuser@gmail.com")
                .password("admin")
                .firstName("Jane")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();

        testEntityManager.persistAndFlush(testUser);

        account1 = Account.builder()
                .name("Test Account 1")
                .accountType(Account.AccountType.CASH)
                .balance(BigDecimal.valueOf(1000))
                .currency(Account.Currency.USD)
                .user(testUser)
                .build();

        account2 = Account.builder()
                .name("Test Account 2")
                .accountType(Account.AccountType.CHECKING)
                .balance(BigDecimal.valueOf(32000))
                .currency(Account.Currency.JPY)
                .user(testUser)
                .build();
    }

    @Test
    void testFindByUserId_whenUserIdIsValid_thenReturnAccountsList() {

        // Arrange
        testEntityManager.persist(account1);
        testEntityManager.persist(account2);
        testEntityManager.flush();

        // Act
        List<Account> accountList = accountRepository.findByUserId(testUser.getId());

        // Assert
        assertThat(accountList).isNotNull();
        assertThat(accountList).isNotEmpty();
        assertThat(accountList.getFirst().getName()).isEqualTo(account1.getName());
        assertThat(accountList.get(1).getName()).isEqualTo(account2.getName());
        assertThat(accountList).hasSize(2);
    }

    @Test
    void testFindByUserId_whenUserIdIsNotFound_thenReturnEmptyAccountList() {

        // Arrange
        Long nonExistentUserId = 999L;

        // Act
        List<Account> accountList = accountRepository.findByUserId(nonExistentUserId);

        // Assert
        assertThat(accountList).isNotNull();
        assertThat(accountList).isEmpty();
    }

}
