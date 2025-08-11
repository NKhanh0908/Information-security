package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptMapper {
    public LoginAttemptDTO entityToDTO(LoginAttempt loginAttempt){
        return LoginAttemptDTO.builder()
                .attemptId(loginAttempt.getAttemptId())
                .accountId(loginAttempt.getAccount().getAccountId())
                .attemptSuccess(loginAttempt.getAttemptSuccess())
                .attemptUserAgent(loginAttempt.getAttemptUserAgent())
                .attemptFailureReason(loginAttempt.getAttemptFailureReason() != null ? loginAttempt.getAttemptFailureReason() : null)
                .attemptCreatedAt(loginAttempt.getAttemptCreatedAt() != null ? loginAttempt.getAttemptCreatedAt().toString() : null)
                .build();
    }
}
