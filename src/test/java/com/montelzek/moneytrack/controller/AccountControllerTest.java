package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@ExtendWith(MockitoExtension.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    void listAccount_shouldReturnListViewWithAccounts() throws Exception {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(1L);
        when(accountService.findUsersAccounts(1L))
                .thenReturn(Arrays.asList(new Account(), new Account()));

        // Act & Assert
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/list"))
                .andExpect(model().attributeExists("account", "accountTypes", "currencies", "accounts"));

        verify(userService, times(1)).getCurrentUserId();
        verify(accountService, times(1)).findUsersAccounts(1L);
    }
}
