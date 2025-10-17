package com.infomationsecurity.mfa.dto.request.accountDTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FormVerify {
    private String username;

    private String password;
}
