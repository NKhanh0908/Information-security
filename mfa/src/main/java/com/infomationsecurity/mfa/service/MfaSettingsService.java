package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
public interface MfaSettingsService {
    CompletableFuture<MfaSettings> create(MfaSettings mfaSettings);

    MfaSettingsDTO update(MfaSettings mfaSettings);

    MfaSettingsDTO getMfaSettingsByAccountId(Integer accountId);

    MfaSettings getMfaSettingsByAccount(Integer accountId);

    AuthenticationDTO verifyLoginWithTOTP(VerifyDeviceWithTOTP verifyDeviceWithTOTP);

    void updateSecretKey(String secretKey);

    RequestInfo extractRequestInfo();

    String getTotpSecretKey(Integer accountId);
}
