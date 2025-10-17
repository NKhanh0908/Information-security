package com.infomationsecurity.mfa.service.nonAuth.impl;

import com.infomationsecurity.mfa.dto.request.nonAuth.totpDTO.TOTPVerificationAuth;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.service.TOTPService;
import com.infomationsecurity.mfa.service.nonAuth.NATOTPService;
import com.infomationsecurity.mfa.util.TOTPUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NATOTPServiceImpl implements NATOTPService {
    private final TOTPUtil totpUtil;
    private final AccountService accountService;
    private final TOTPService totpService;
    private final MfaSettingsService mfaSettingsService;


    /**
     * @param totpVerificationAuth
     * @return
     */
    @Override
    public Boolean verifyTOTP(TOTPVerificationAuth totpVerificationAuth) {
        Optional<Account> account = accountService.getAccountByEmail(totpVerificationAuth.getEmail());
        if (account.isPresent()) {
            MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(account.get().getAccountId());
            String secretKey = mfaSettings.getMfaTotpSecretKey();
            return totpService.verifyTOTP(totpVerificationAuth.getOtp(), secretKey);
        }
        return null;
    }
}
