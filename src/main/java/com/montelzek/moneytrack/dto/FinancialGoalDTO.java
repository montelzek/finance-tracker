package com.montelzek.moneytrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialGoalDTO {

    @NotBlank(message = "Name can't be empty.")
    @Size(max = 255, message = "Max 255 characters.")
    private String name;

    @NotNull(message = "Budget size can't be empty.")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private BigDecimal targetAmount;

    private Long id;
}
