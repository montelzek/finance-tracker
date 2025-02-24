package com.montelzek.moneytrack.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterDTO {

    @NotBlank(message = "Can't be blank.")
    @Email
    @Size(max = 50, message = "Max 50 characters.")
    private String email;

    @NotBlank(message = "Can't be blank.")
    @Size(max = 120, message = "Max 120 characters.")
    private String password;

    @Size(max = 50, message = "Max 50 characters.")
    private String firstName;

    @Size(max = 50, message = "Max 50 characters.")
    private String lastName;
}
