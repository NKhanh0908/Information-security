package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.VerifyOTP;
import com.infomationsecurity.mfa.dto.response.VerificationResult;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;

public interface AuthenticationService {
    AuthenticationDTO signIn(FormVerify formVerify);

    Boolean verifyPassword(String rawPassword);

    AuthenticationDTO authWithGitHub(String authorizationCode);

    AuthenticationDTO processSuccessfulLogin(Account account, RequestInfo requestInfo, String username);

    void sendEmailNotificationVerify();
    Boolean verifyOtp(VerifyOTP verifyOTP);

    VerificationResult verifyEmail(EmailVerificationDTO emailVerificationDTO);
}
