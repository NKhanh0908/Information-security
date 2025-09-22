package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailResendOTP;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDevice;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendVerificationOTPEmail(EmailResendOTP emailResendOTP);

    void sendEmailVerifyDevice(FormVerify formVerify);

    Boolean verifyEmail(EmailVerificationDTO emailVerificationDTO);

    Boolean verifiedSignUp(EmailVerificationDTO emailVerificationDTO);

    Boolean verifyEmailDevice(EmailVerificationDevice emailVerificationDevice);
}
