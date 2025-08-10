package com.infomationsecurity.mfa.enums;

import lombok.Getter;

@Getter
public enum MfaMethod {
    TOTP("TOTP"),
    EMAIL("EMAIL"),
    WEBAUTHN("WEBAUTHN"),
    AUTHENTICATOR_APP("AUTHENTICATOR_APP"),
    BACKUP_CODES("BACKUP_CODES");

    private final String value;

    MfaMethod(String value) {
        this.value = value;
    }

}
