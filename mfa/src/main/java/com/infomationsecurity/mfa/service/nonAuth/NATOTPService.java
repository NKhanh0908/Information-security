package com.infomationsecurity.mfa.service.nonAuth;

import com.infomationsecurity.mfa.dto.request.nonAuth.totpDTO.TOTPVerificationAuth;
import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import org.springframework.stereotype.Service;

@Service
public interface NATOTPService {
    Boolean verifyTOTP(TOTPVerificationAuth totpVerificationAuth);
}
