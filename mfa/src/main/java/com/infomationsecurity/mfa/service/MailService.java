package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import org.springframework.stereotype.Service;

@Service
public interface MailService {
    void sendVerificationOTPEmail(String verificationEmail);

    Boolean verifyEmail(EmailVerificationDTO emailVerificationDTO);

    Boolean verifiedSignUp(EmailVerificationDTO emailVerificationDTO);
    
}
