package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.config.SecurityConfig;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    public void setup() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .roles(new HashSet<>())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void listUsers_adminUser_shouldShowUserList() throws Exception {
        // Arrange
        when(userService.findAll()).thenReturn(List.of(testUser));

        // Act & Assert
        mockMvc.perform(get("/admin-panel"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-panel"))
                .andExpect(model().attributeExists("ERole"))
                .andExpect(model().attributeExists("users"));

        verify(userService).findAll();
    }

    @Test
    @WithMockUser
    void showReports_userIsNotAdmin_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/admin-panel"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void deleteUser_adminUser_shouldDeleteAndRedirect() throws Exception {
        // Arrange
        doNothing().when(userService).deleteById(testUser.getId());

        // Act & Arrange
        mockMvc.perform(get("/user/delete")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin-panel"));

        verify(userService).deleteById(testUser.getId());
    }

    @Test
    @WithMockUser
    public void deleteUser_userIsNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/user/delete")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isForbidden());

        verify(userService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void grantPremium_adminUser_shouldGrantAndRedirect() throws Exception {
        // Arrange
        doNothing().when(userService).grantPremiumRole(testUser.getId());

        // Act & Assert
        mockMvc.perform(post("/user/grant-premium")
                        .param("userId", String.valueOf(testUser.getId()))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin-panel"));

        verify(userService).grantPremiumRole(testUser.getId());
    }

    @Test
    @WithMockUser
    public void grantPremium_userIsNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/user/grant-premium")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isForbidden());

        verify(userService, never()).grantPremiumRole(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void revokePremium_adminUser_shouldRevokeAndRedirect() throws Exception {
        // Arrange
        doNothing().when(userService).revokePremiumRole(testUser.getId());

        // Act & Assert
        mockMvc.perform(post("/user/revoke-premium")
                        .param("userId", String.valueOf(testUser.getId()))
                        .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/admin-panel"));

        verify(userService).revokePremiumRole(testUser.getId());
    }

    @Test
    @WithMockUser
    public void revokePremium_userIsNotAdmin_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/user/revoke-premium")
                        .param("userId", String.valueOf(testUser.getId())))
                .andExpect(status().isForbidden());

        verify(userService, never()).revokePremiumRole(anyLong());
    }
}
