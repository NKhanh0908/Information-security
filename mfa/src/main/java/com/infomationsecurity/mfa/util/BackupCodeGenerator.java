package com.infomationsecurity.mfa.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BackupCodeGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static String generateBackupCode() {
        byte[] randomBytes = new byte[6]; // 6 bytes ≈ 8 ký tự base32/base64
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public static List<String> generateBackupCodes(int count) {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            codes.add(generateBackupCode());
        }
        return codes;
    }
}
