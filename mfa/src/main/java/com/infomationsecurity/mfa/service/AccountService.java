package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    AuthenticationDTO signIn(FormLoginDTO formLoginDTO);

    AccountDTO signUp(AccountCreateDTO accountCreateDTO);

    AccountDTO getAccountAuth();

    //ForgotPasswordResponseDTO forgotPassword(String email);

    //OtpVerificationResponseDTO verifyOtp(OtpVerificationDTO otpVerificationDTO);

    //String resetPassword(ResetPasswordDTO resetPasswordDTO);
}
