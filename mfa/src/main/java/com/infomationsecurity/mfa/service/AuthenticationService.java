package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;

public interface AuthenticationService {
    AuthenticationDTO signIn(FormLoginDTO formLoginDTO);

    Boolean verifyPassword(String rawPassword);

    AuthenticationDTO authWithGitHub(String authorizationCode);

    AuthenticationDTO processSuccessfulLogin(Account account, RequestInfo requestInfo, String username);
}
