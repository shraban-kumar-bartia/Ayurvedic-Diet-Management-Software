package com.sih.ayurvedic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class RegisterDto {

    @NotEmpty
    private String firstname;

    @NotEmpty
    private String lastname;

    private String username;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String password;

    private String phoneNumber;
}
