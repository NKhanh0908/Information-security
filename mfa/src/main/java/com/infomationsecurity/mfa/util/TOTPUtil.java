package com.infomationsecurity.mfa.util;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
public class TOTPUtil {

    private static final int SECRET_SIZE = 20;
    private static final String RANDOM_NUMBER_ALGORITHM = "SHA1PRNG";

    /**
     * Tạo secret key mới cho user
     */
    public String generateSecretKey() {
        SecureRandom sr;
        try {
            sr = SecureRandom.getInstance(RANDOM_NUMBER_ALGORITHM);
            byte[] buffer = new byte[SECRET_SIZE];
            sr.nextBytes(buffer);
            Base32 codec = new Base32();
            return codec.encodeToString(buffer);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Không thể tạo secret key", e);
        }
    }

    /**
     * Tạo TOTP code dựa trên secret key và timestamp
     */
    public String getTOTPCode(String secretKey, long timeSlice) {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secretKey.getBytes());

        long time = timeSlice / 30000;

        byte[] msg = new byte[8];
        for (int i = 0; i < 8; i++) {
            msg[7 - i] = (byte) (time >>> (i * 8));
        }

        byte[] hash = hmacSha1(decodedKey, msg);

        int offset = hash[hash.length - 1] & 0xf;
        int binary = ((hash[offset] & 0x7f) << 24) |
                ((hash[offset + 1] & 0xff) << 16) |
                ((hash[offset + 2] & 0xff) << 8) |
                (hash[offset + 3] & 0xff);

        int otp = binary % 1000000;
        return String.format("%06d", otp);
    }

    /**
     * Xác thực TOTP code
     */
    public boolean verifyCode(String secretKey, String code, int variance) {
        long timeSlice = System.currentTimeMillis();

        for (int i = -variance; i <= variance; i++) {
            long time = timeSlice + (i * 30000);
            String generatedCode = getTOTPCode(secretKey, time);
            if (generatedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tạo QR Code URL cho Google Authenticator
     */
    public String getQRBarcodeURL(String user, String host, String secret, String domain) {
        return String.format(
                "otpauth://totp/%s@%s?secret=%s&issuer=%s",
                user, host, secret, domain
        );
    }

    private byte[] hmacSha1(byte[] keyBytes, byte[] text) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(text);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi khi tạo HMAC", e);
        }
    }
}