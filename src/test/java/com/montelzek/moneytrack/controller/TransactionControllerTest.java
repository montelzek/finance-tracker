package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.*;
import com.montelzek.moneytrack.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@ExtendWith(MockitoExtension.class)
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private FinancialGoalService financialGoalService;

    private User testUser;
    private Category category;
    private Account account;
    private Transaction transaction;
    private TransactionDTO transactionDTO;

    @BeforeEach
    public void setup() {
        testUser = User.builder().id(1L).build();

        category = Category.builder().id(1).build();

        account = Account.builder().id(1L).build();

        transaction = Transaction.builder()
                .id(1L)
                .category(category)
                .amount(BigDecimal.valueOf(200))
                .account(account)
                .date(LocalDate.now())
                .build();

        transactionDTO = TransactionDTO.builder()
                .id(1L)
                .categoryId(category.getId())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(200))
                .date(LocalDate.now())
                .build();
    }

    @Test
    @WithMockUser
    public void listTransactions_shouldReturnListViewWithPagedTransactions() throws Exception {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> transactionPage = new PageImpl<>(Collections.singletonList(transaction), pageable, 1);

        when(userService.getCurrentUserId()).thenReturn(1L);
        when(transactionService.findAccountsTransactions(1L, pageable)).thenReturn(transactionPage);
        when(categoryService.findByType("INCOME")).thenReturn(Collections.singletonList(category));
        when(categoryService.findByType("EXPENSE")).thenReturn(Collections.singletonList(category));
        when(categoryService.findByType("FINANCIAL_GOAL")).thenReturn(Collections.singletonList(category));
        when(accountService.findUsersAccounts(1L)).thenReturn(Collections.singletonList(account));
        when(financialGoalService.findUsersFinancialGoals(1L)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/transactions")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions/list"))
                .andExpect(model().attributeExists("transactionsPage"))
                .andExpect(model().attributeExists("transaction"))
                .andExpect(model().attributeExists("incomeCategories"))
                .andExpect(model().attributeExists("expenseCategories"))
                .andExpect(model().attributeExists("financialGoalCategories"))
                .andExpect(model().attributeExists("financialGoals"))
                .andExpect(model().attributeExists("accounts"));

        verify(userService, times(2)).getCurrentUserId();
        verify(transactionService).findAccountsTransactions(1L, pageable);
        verify(categoryService).findByType(eq("INCOME"));
        verify(categoryService).findByType(eq("EXPENSE"));
        verify(categoryService).findByType(eq("FINANCIAL_GOAL"));
        verify(accountService).findUsersAccounts(1L);
        verify(financialGoalService).findUsersFinancialGoals(1L);
    }

    @Test
    @WithMockUser
    public void saveTransaction_dataIsValidAndIdIsNull_shouldCreateTransaction() throws Exception {
        // Arrange
        String source = "dashboard";
        transactionDTO.setId(null);
        doNothing().when(transactionService).createTransaction(transactionDTO);

        // Act & Assert
        mockMvc.perform(post("/transactions/save")
                .flashAttr("transaction", transactionDTO)
                .param("source", source)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + source));

        verify(transactionService).createTransaction(transactionDTO);
        verify(transactionService, never()).updateTransaction(transactionDTO);
    }

    @Test
    @WithMockUser
    public void saveTransaction_dataIsValidAndIdIsNotNull_shouldUpdateTransaction() throws Exception {
        // Arrange
        String source = "transactions";
        doNothing().when(transactionService).updateTransaction(transactionDTO);

        // Act & Assert
        mockMvc.perform(post("/transactions/save")
                        .flashAttr("transaction", transactionDTO)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + source));

        verify(transactionService).updateTransaction(transactionDTO);
        verify(transactionService, never()).createTransaction(transactionDTO);
    }

    @Test
    @WithMockUser
    public void saveTransaction_invalidData_shouldRedirect() throws Exception{
        // Arrange
        String source = "transactions";
        transactionDTO.setAmount(null);

        // Act & Arrange
        mockMvc.perform(post("/transactions/save")
                .flashAttr("transaction", transactionDTO)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + source))
                .andExpect(flash().attributeExists("org.springframework.validation.BindingResult.transaction"))
                .andExpect(flash().attributeExists("transaction"));

        verify(transactionService, never()).createTransaction(any());
        verify(transactionService, never()).updateTransaction(any());
    }

    @Test
    @WithMockUser
    public void saveTransaction_serviceThrowsIllegalArgumentException_shouldRedirect() throws Exception{
        // Arrange
        String source = "transactions";
        String errorMessage = "Error Message";

        TransactionDTO dtoForCreate = TransactionDTO.builder()
                .categoryId(category.getId())
                .accountId(account.getId())
                .amount(BigDecimal.valueOf(150))
                .date(LocalDate.now())
                .build();

        doThrow(new IllegalArgumentException(errorMessage))
                .when(transactionService).createTransaction(dtoForCreate);

        // Act & Assert
        mockMvc.perform(post("/transactions/save")
                        .flashAttr("transaction", dtoForCreate)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/" + source))
                .andExpect(flash().attributeExists("error"));

        verify(transactionService).createTransaction(dtoForCreate);
        verify(transactionService, never()).updateTransaction(any());
    }

    @Test
    @WithMockUser
    public void deleteTransaction_successfulDeletion_shouldRedirectToTransactions() throws Exception {
        // Arrange
        doNothing().when(transactionService).deleteById(transaction.getId());

        // Act & Assert
        mockMvc.perform(get("/transactions/delete")
                        .param("transactionId", String.valueOf(transaction.getId())))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        verify(transactionService).deleteById(transaction.getId());
    }

    @Test
    @WithMockUser
    public void getTransactionForEdit_shouldReturnTransactionDTOasJson() throws Exception {
        // Arrange
        when(transactionService.findById(1L)).thenReturn(transaction);
        when(transactionService.convertToDTO(transaction)).thenReturn(transactionDTO);

        // Act & Assert
        mockMvc.perform(get("/transactions/edit/{id}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(BigDecimal.valueOf(200)));

        verify(transactionService).findById(1L);
        verify(transactionService).convertToDTO(transaction);
    }
}
