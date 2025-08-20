package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.fiters.LoginAttemptFilter;
import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface LoginAttemptService {
    LoginAttemptDTO create(LoginAttempt loginAttempt);

    CompletableFuture<LoginAttemptDTO> saveSuccessfulLoginAttempt(Account account, TrustDevice trustDevice, String userAgent);

    CompletableFuture<LoginAttemptDTO> saveFailedLoginAttempt(Account account, TrustDevice trustDevice, String userAgent, String failureReason);

    List<LoginAttemptDTO> getLoginAttemptByAccount();

    List<LoginAttemptDTO> getLoginAttemptByTrustDeviceId(Integer trustDeviceId);

    PageDTO<LoginAttemptDTO> filter(LoginAttemptFilter filter, Integer page, Integer size);

}
