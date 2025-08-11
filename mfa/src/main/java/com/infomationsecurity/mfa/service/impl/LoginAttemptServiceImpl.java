package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.mapper.LoginAttemptMapper;
import com.infomationsecurity.mfa.repository.LoginAttemptRepository;
import com.infomationsecurity.mfa.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    private final LoginAttemptMapper loginAttemptMapper;

    @Override
    public LoginAttempt create(LoginAttempt loginAttempt) {
        log.info("Creating login attempt for account ID: {}", loginAttempt.getAccount().getAccountId());

        return loginAttemptRepository.save(loginAttempt);
    }

    @Override
    public LoginAttemptDTO update(LoginAttempt loginAttempt) {
        return null;
    }

    @Override
    public LoginAttemptDTO getLoginAttemptByAccountId(Integer accountId) {
        return null;
    }
}
