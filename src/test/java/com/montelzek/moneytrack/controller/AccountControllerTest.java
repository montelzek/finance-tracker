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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

        verify(userService).getCurrentUserId();
        verify(accountService).findUsersAccounts(testUser.getId());
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

        verify(accountService).findById(1L);
        verify(accountService).convertToDTO(account1);
    }

    @Test
    @WithMockUser
    public void saveAccount_successfulSave_shouldRedirectToAccount() throws Exception {
        // Arrange
        doNothing().when(accountService).saveAccount(accountDTO);

        // Act & Assert
        mockMvc.perform(post("/accounts/save")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("account", accountDTO))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(accountService).saveAccount(accountDTO);
    }

    @Test
    @WithMockUser
    public void saveAccount_invalidData_shouldReturnAccountListView() throws Exception {
        // Arrange
        accountDTO.setAccountType(null);

        // Act & Assert
        mockMvc.perform(post("/accounts/save")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .flashAttr("account", accountDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/list"))
                .andExpect(model().attributeHasFieldErrors("account", "accountType"));

        verify(accountService, never()).saveAccount(accountDTO);
    }

    @Test
    @WithMockUser
    public void saveAccount_illegalArgumentException_shouldReturnListWithViewWithError() throws Exception{
        // Arrange
        doThrow(new IllegalArgumentException()).when(accountService).saveAccount(accountDTO);

        // Act & Arrange
        mockMvc.perform(post("/accounts/save")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("account", accountDTO))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/list"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("account"));

        verify(accountService).saveAccount(accountDTO);
    }

    @Test
    @WithMockUser
    public void deleteAccount_successfulDeletion_shouldRedirectToAccounts() throws Exception {
        // Arrange
        doNothing().when(accountService).deleteById(account1.getId());

        // Act & Assert
        mockMvc.perform(get("/accounts/delete")
                .param("accountId", String.valueOf(account1.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        verify(accountService).deleteById(account1.getId());
    }
}
