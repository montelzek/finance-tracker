package com.montelzek.moneytrack.repository;

import com.montelzek.moneytrack.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/data.sql")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void testFindByName_GivenExistingRole_ShouldReturnRole() {
        // Act
        Optional<Role> foundUserRole = roleRepository.findByName(Role.ERole.ROLE_USER);
        Optional<Role> foundAdminRole = roleRepository.findByName(Role.ERole.ROLE_ADMIN);
        Optional<Role> foundPremiumRole = roleRepository.findByName(Role.ERole.ROLE_PREMIUM);

        // Assert
        assertThat(foundUserRole).isPresent();
        assertThat(foundUserRole.get().getName()).isEqualTo(Role.ERole.ROLE_USER);

        assertThat(foundAdminRole).isPresent();
        assertThat(foundAdminRole.get().getName()).isEqualTo(Role.ERole.ROLE_ADMIN);

        assertThat(foundPremiumRole).isPresent();
        assertThat(foundPremiumRole.get().getName()).isEqualTo(Role.ERole.ROLE_PREMIUM);
    }

    @Test
    public void testFindByName_GivenNonExistingRole_ShouldReturnEmptyOptional() {
        // Act
        Optional<Role> nonExistingRole = roleRepository.findByName(null);

        // Assert
        assertThat(nonExistingRole).isEmpty();
    }
}
