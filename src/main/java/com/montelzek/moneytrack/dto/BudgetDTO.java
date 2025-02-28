package com.montelzek.moneytrack.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class BudgetDTO {

    @NotBlank(message = "Name can't be empty.")
    @Size(max = 255, message = "Max 255 characters.")
    private String name;

    @NotNull(message = "Start date can't be empty.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date can't be empty.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @NotNull(message = "Budget size can't be empty.")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double budgetSize;

    private Long id;

    private String categoryType;
}
