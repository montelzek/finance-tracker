package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.AccountDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.User;
import com.montelzek.moneytrack.service.AccountService;
import com.montelzek.moneytrack.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

    private User testUser;
    private Account account1;
    private Account account2;
    private AccountDTO accountDTO;

    @BeforeEach
    public void setup() {
        testUser = User.builder().id(1L).build();

        account1 = Account.builder()
                .id(1L)
                .name("Test Account 1")
                .accountType(Account.AccountType.CASH)
                .currency(Account.Currency.USD)
                .balance(BigDecimal.valueOf(1000))
                .user(testUser)
                .build();

        account2 = Account.builder()
                .id(2L)
                .name("Test Account 2")
                .accountType(Account.AccountType.CHECKING)
                .currency(Account.Currency.PLN)
                .balance(BigDecimal.valueOf(4000))
                .user(testUser)
                .build();

        accountDTO = AccountDTO.builder()
                .id(1L)
                .name("Test Account 1")
                .accountType(Account.AccountType.CASH)
                .currency(Account.Currency.USD)
                .balance(BigDecimal.valueOf(1000))
                .build();

    }

    @Test
    @WithMockUser
    public void listAccount_shouldReturnListViewWithAccounts() throws Exception {
        // Arrange
        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(accountService.findUsersAccounts(testUser.getId()))
                .thenReturn(Arrays.asList(account1, account2));

        // Act & Assert
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/list"))
                .andExpect(model().attributeExists("account", "accountTypes", "currencies", "accounts"));

        verify(userService, times(1)).getCurrentUserId();
        verify(accountService, times(1)).findUsersAccounts(testUser.getId());
    }

    @Test
    @WithMockUser
    public void getAccountForEdit_shouldReturnAccountDTOasJson() throws Exception {
        // Arrange
        when(accountService.findById(1L)).thenReturn(account1);
        when(accountService.convertToDTO(account1)).thenReturn(accountDTO);

        // Act & Assert
        mockMvc.perform(get("/accounts/edit/{id}", 1L)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Test Account 1"));

        verify(accountService, times(1)).findById(1L);
        verify(accountService, times(1)).convertToDTO(account1);
    }
}
