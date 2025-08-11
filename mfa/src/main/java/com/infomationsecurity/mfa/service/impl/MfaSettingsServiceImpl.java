package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.mapper.MfaSettingsMapper;
import com.infomationsecurity.mfa.repository.MfaSettingsRepository;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class MfaSettingsServiceImpl implements MfaSettingsService {
    private final MfaSettingsRepository mfaSettingsRepository;

    private final MfaSettingsMapper mfaSettingsMapper;

    @Override
    public MfaSettings create(MfaSettings mfaSettings) {
        log.info("Creating MFA settings for account ID: {}", mfaSettings.getAccount().getAccountId());

        return mfaSettingsRepository.save(mfaSettings);
    }

    @Override
    public MfaSettingsDTO update(MfaSettings mfaSettings) {
        return null;
    }

    @Override
    public MfaSettingsDTO getMfaSettingsByAccountId(Integer accountId) {
        return null;
    }
}
