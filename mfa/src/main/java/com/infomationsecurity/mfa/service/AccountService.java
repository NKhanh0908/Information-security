package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    AuthenticationDTO signIn(FormLoginDTO formLoginDTO);

    AccountDTO signUp(AccountCreateDTO accountCreateDTO);

    AccountDTO signUpWithGoogle();

    AuthenticationDTO authWithGitHub(String authorizationCode);

    AccountDTO getAccountAuth();

    AuthenticationDTO refreshToken(RefreshTokenDTO refreshTokenDTO);

    //ForgotPasswordResponseDTO forgotPassword(String email);

    //OtpVerificationResponseDTO verifyOtp(OtpVerificationDTO otpVerificationDTO);

    //String resetPassword(ResetPasswordDTO resetPasswordDTO);

    AuthenticationDTO verifyLoginWithTOTP(VerifyDeviceWithTOTP verifyDeviceWithTOTP);
}
