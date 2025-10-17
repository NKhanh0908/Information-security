package com.infomationsecurity.mfa.dto.request.nonAuth.totpDTO;

import lombok.Data;

@Data
public class TOTPVerificationAuth {
    private String email;
    private String otp;
}
