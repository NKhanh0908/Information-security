package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.stereotype.Service;

@Service
public interface LoginAttemptService {
    LoginAttempt create(Account account, TrustDevice trustDevice, String userAgent);

    LoginAttemptDTO update(LoginAttempt loginAttempt);

    LoginAttemptDTO getLoginAttemptByAccountId(Integer accountId);

}
