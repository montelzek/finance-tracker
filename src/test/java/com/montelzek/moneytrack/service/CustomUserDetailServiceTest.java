package com.montelzek.moneytrack.service;

import com.montelzek.moneytrack.model.Role;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsername_existingUser_shouldReturnUserDetails() {
        // Arrange
        Role role = new Role();
        role.setName(Role.ERole.ROLE_USER);

        User user = User.builder()
                .email("user@gmail.com")
                .password("admin")
                .roles(Set.of(role))
                .build();
        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("user@gmail.com");

        // Assert
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(user.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(user.getPassword());
        verify(userRepository).findByEmail("user@gmail.com");
    }

    @Test
    public void loadUserByUsername_userNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@gmail.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatExceptionOfType(UsernameNotFoundException.class)
                        .isThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@gmail.com"))
                        .withMessage("User not find with email: nonexistent@gmail.com");

        verify(userRepository).findByEmail("nonexistent@gmail.com");
    }
}
