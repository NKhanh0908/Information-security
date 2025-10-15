package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.response.TOTPRegistrationDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import org.springframework.stereotype.Service;

@Service
public interface TOTPService {
    TOTPRegistrationDTO registerTOTP();

    Boolean verifyRegisterTOTP(TOTPVerificationDTO verificationDTO);

    Boolean verifyTOTP(TOTPVerificationDTO verificationDTO);

    Boolean verifyTOTP(String code, String secretKey);

    Boolean verifyLoginRequest(AccountDTO account, String totpCode);

    String[] generateBackupCodes(AccountDTO account);

    Boolean verifyBackupCode(AccountDTO account, String backupCode);
}
