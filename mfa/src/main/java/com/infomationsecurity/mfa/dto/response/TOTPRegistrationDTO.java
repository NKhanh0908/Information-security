package com.infomationsecurity.mfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TOTPRegistrationDTO {
    private String secretKey;
    private String qrCodeUrl;
    private String qrCodeImage; // Base64 encoded image
    private String[] backupCodes;
    private String message;
}