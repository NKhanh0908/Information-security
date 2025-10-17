package com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO;

import lombok.Data;

@Data
public class FormResetPasswordDTO {
    private String email;
    private String password;
}
