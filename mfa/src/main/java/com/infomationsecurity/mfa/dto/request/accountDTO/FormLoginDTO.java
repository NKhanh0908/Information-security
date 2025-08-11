package com.infomationsecurity.mfa.dto.request.accountDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormLoginDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
