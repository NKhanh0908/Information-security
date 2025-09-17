package com.infomationsecurity.mfa.dto.response.accountDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO {
    private String token;
    private String refreshToken;
    private String role;
    // MFA related fields
    private Boolean mfaRequired = false;
    private Integer deviceId;
    private String username;
    private String message;

    // Optional: MFA challenge details
    private String mfaMethod; // EMAIL, TOTP, AUTHENTICATOR_APP, etc.
    private String challengeId; // For tracking MFA challenge

    // For backwards compatibility
    public AuthenticationDTO(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.mfaRequired = false;
    }

    // Helper methods for creating different response types
    public static AuthenticationDTO successfulLogin(String token, String refreshToken) {
        return AuthenticationDTO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .mfaRequired(false)
                .build();
    }

    public static AuthenticationDTO mfaRequired(Integer deviceId, String message, String mfaMethod) {
        return AuthenticationDTO.builder()
                .token(null)
                .refreshToken(null)
                .mfaRequired(true)
                .deviceId(deviceId)
                .message(message)
                .mfaMethod(mfaMethod)
                .build();
    }
}
