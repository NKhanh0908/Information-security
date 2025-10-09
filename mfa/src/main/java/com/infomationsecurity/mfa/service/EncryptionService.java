package com.infomationsecurity.mfa.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct; // gói javax.annotation
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int IV_SIZE = 12; // bytes recommended for GCM

    @Value("${app.encryption.key}")
    private String base64Key; // expects base64-encoded 16 bytes (AES-128)

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64Key);
            if (keyBytes.length != 16) {
                throw new IllegalArgumentException("Encryption key must be 16 bytes for AES-128");
            }
            secretKey = new SecretKeySpec(keyBytes, AES);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize EncryptionService: " + ex.getMessage(), ex);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Prepend IV to ciphertext: final = iv || ciphertext
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String base64IvAndCipherText) {
        if (base64IvAndCipherText == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(base64IvAndCipherText);

            if (combined.length < IV_SIZE) {
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            int cipherTextLen = combined.length - IV_SIZE;
            byte[] cipherText = new byte[cipherTextLen];
            System.arraycopy(combined, IV_SIZE, cipherText, 0, cipherTextLen);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
