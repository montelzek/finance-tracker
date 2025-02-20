package com.montelzek.moneytrack.dto;

import com.montelzek.moneytrack.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AccountDTO {

    private Long id;

    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private Account.AccountType accountType;

    @NotNull
    private Account.Currency currency;

    @NotNull
    private Double balance;

    private LocalDateTime createdAt;
}
