package com.montelzek.moneytrack.controller;

import com.montelzek.moneytrack.dto.TransactionDTO;
import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import com.montelzek.moneytrack.model.Transaction;
import com.montelzek.moneytrack.model.User;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
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
}
