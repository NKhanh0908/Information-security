package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailResendOTP;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDevice;
import com.infomationsecurity.mfa.dto.response.VerificationResult;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendVerificationOTPEmail(EmailResendOTP emailResendOTP);

    void sendEmailVerifyDevice(FormVerify formVerify);

    VerificationResult verifyEmail(EmailVerificationDTO emailVerificationDTO);

    VerificationResult verifiedSignUp(EmailVerificationDTO emailVerificationDTO);

    Boolean verifyEmailDevice(EmailVerificationDevice emailVerificationDevice);
}
