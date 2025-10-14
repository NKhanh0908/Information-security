package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.request.setting.MfaSettingUpdate;
import com.infomationsecurity.mfa.dto.response.settingDTO.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public interface MfaSettingsService {
    CompletableFuture<MfaSettings> create(MfaSettings mfaSettings);

    MfaSettingsDTO update(MfaSettingUpdate mfaSettingUpdate, Integer mfaId);

    MfaSettingsDTO getMfaSettingsByAccount(FormVerify formVerify);

    MfaSettingsDTO getMfaSettingCurrentUser();

    MfaSettings getMfaSettingsByAccount(Integer accountId);

    AuthenticationDTO verifyLoginWithTOTP(VerifyDeviceWithTOTP verifyDeviceWithTOTP);

    void updateSecretKey(String secretKey);

    RequestInfo extractRequestInfo();

    String getTotpSecretKey(Integer accountId);
}
