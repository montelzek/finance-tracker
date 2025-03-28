package com.montelzek.moneytrack.dto;

import com.montelzek.moneytrack.model.Account;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {

    private Long id;

    @NotBlank(message = "Can't be blank.")
    @Size(max = 120, message = "Max 120 characters.")
    private String name;

    @NotNull(message = "Can't be null.")
    private Account.AccountType accountType;

    @NotNull(message = "Can't be null.")
    private Account.Currency currency;

    @NotNull(message = "Can't be null.")
    private BigDecimal balance;

    private LocalDateTime createdAt;
}
