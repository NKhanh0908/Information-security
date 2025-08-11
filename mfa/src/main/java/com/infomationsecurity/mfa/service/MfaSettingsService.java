package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.stereotype.Service;

@Service
public interface MfaSettingsService {
    MfaSettingsDTO create(MfaSettings mfaSettings);

    MfaSettingsDTO update(MfaSettings mfaSettings);

    MfaSettingsDTO getMfaSettingsByAccountId(Integer accountId);

}
