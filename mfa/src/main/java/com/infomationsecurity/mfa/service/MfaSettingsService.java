package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface MfaSettingsService {
    MfaSettings create(MfaSettings mfaSettings);

    MfaSettingsDTO update(MfaSettings mfaSettings);

    void getMfaSettingsByAccountId(AccountDTO accountDTO);

    MfaSettings getMfaSettingsByAccount(Integer accountId);

    void updateSecretKey(String secretKey);
}
