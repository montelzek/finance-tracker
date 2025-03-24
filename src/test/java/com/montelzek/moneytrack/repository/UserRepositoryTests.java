package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .email("testuser@gmail.com")
                .password("admin")
                .firstName("Jane")
                .lastName("Doe")
                .roles(new HashSet<>())
                .build();
    }

    @Test
    void testFindByEmail_whenEmailIsValid_thenReturnUser() {
        // Arrange
        testEntityManager.persistAndFlush(testUser);

        // Act
        Optional<User> savedUser = userRepository.findByEmail(testUser.getEmail());

        // Assert
        assertThat(savedUser).isPresent();
        User foundUser = savedUser.get();
        assertThat(foundUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void testFindByEmail_whenEmailDoesNotExist_thenReturnEmptyOptional() {
        // Arrange
        testEntityManager.persistAndFlush(testUser);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }
}
