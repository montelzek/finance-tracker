package com.montelzek.moneytrack.dto;

import com.montelzek.moneytrack.model.Account;
import com.montelzek.moneytrack.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class TransactionDTO {

    private Long id;

    private String categoryType;

    @NotNull(message = "Can't be null.")
    private Long accountId;

    @NotNull(message = "Can't be null.")
    @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
    private Double amount;

    @NotNull(message = "Can't be null.")
    private Integer categoryId;

    @Size(max = 255, message = "Max 255 characters.")
    private String description;

    @NotNull(message = "Can't be null.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
}
