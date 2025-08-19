package com.infomationsecurity.mfa.dto.request.totpDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TOTPVerificationDTO {
    private String code;
    private String secretKey;
}