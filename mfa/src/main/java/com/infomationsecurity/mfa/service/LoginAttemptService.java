package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import org.springframework.stereotype.Service;

@Service
public interface LoginAttemptService {
    LoginAttemptDTO create(LoginAttempt loginAttempt);

    LoginAttemptDTO update(LoginAttempt loginAttempt);

    LoginAttemptDTO getLoginAttemptByAccountId(Integer accountId);

}
