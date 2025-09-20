package com.infomationsecurity.mfa.dto.request.accountDTO;

import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import lombok.Data;

@Data
public class VerifyDeviceWithTOTP {
    private String username;
    private Integer deviceId;
    private TOTPVerificationDTO totpVerificationDTO;
}
