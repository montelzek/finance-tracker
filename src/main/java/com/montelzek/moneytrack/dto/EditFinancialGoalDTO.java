package com.montelzek.moneytrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EditFinancialGoalDTO {

    @NotBlank(message = "Name can't be empty.")
    @Size(max = 255, message = "Max 255 characters.")
    private String name;

    @NotNull(message = "Budget size can't be empty.")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double targetAmount;

    @NotNull(message = "Budget size can't be empty.")
    private Double currentAmount;

    private Long id;
}
