package com.infomationsecurity.mfa.service.nonAuth;

import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO.FormRequireNonAuth;
import com.infomationsecurity.mfa.dto.response.VerificationResult;
import org.springframework.stereotype.Service;

@Service
public interface NAMailService {
    void sendEmailRequiredForgotPassword(FormRequireNonAuth formRequireNonAuth);

    VerificationResult verifyEmailRequiredForgotPassword(EmailVerificationDTO emailVerificationDTO);
}
