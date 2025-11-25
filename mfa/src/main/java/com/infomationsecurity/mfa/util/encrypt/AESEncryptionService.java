package com.infomationsecurity.mfa.util.encrypt;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
@Slf4j
@RequiredArgsConstructor
public class AESEncryptionService {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12; // 96 bits
    private static final int GCM_TAG_LENGTH = 128; // 128 bits

    @Value("${app.encryption.key}")
    private String secretKeyString;

    private SecretKey secretKey;

    private final RSAEncryptionService rsaService;

    /**
     * Khởi tạo secret key từ config hoặc generate mới
     */
    @PostConstruct
    public void init() {
        try {
            // Ở đây base64Key (cấu hình app.encryption.key) là giá trị AES key đã bị RSA mã hóa và Base64
            // (tức là kết quả encryptWithPublicKey(aesKeyBase64)).
            String decryptedKey = rsaService.decryptWithPrivateKey(secretKeyString); // rsaService.decryptWithPrivateKey(base64Key) sẽ trả về chuỗi Base64 gốc của AES key (ví dụ qTmXPNmA6BecHDQ5Mt9QEA==).

            // Sau đó mới Base64.decode để lấy 16 bytes thực sự, rồi tạo SecretKeySpec.
            byte[] keyBytes = Base64.getDecoder().decode(decryptedKey); // Giải Base64 base64Key → keyBytes.
            if (keyBytes.length != 16) { // Kiểm tra chiều dài == 16 bytes (AES-128). Nếu không đúng ném lỗi
                throw new IllegalArgumentException("Encryption key must be 16 bytes for AES-128");
            }
            secretKey = new SecretKeySpec(keyBytes, ALGORITHM);  // Tạo SecretKeySpec từ keyBytes với thuật toán "AES".
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize EncryptionService: " + ex.getMessage(), ex);
        }
    }

    /**
     * Mã hóa văn bản
     * Format: IV (12 bytes) + Encrypted Data + Auth Tag
     */
    public String encrypt(String plainText) throws Exception {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        if (secretKey == null) {
            init();
        }

        // Generate random IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        log.info("Key IV: {}", iv);

        // Mã hóa
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        log.info("Cipher: {}", cipher);

        byte[] encryptedData = cipher.doFinal(plainText.getBytes("UTF-8"));
        log.info("byte[] encryptedData: {}", encryptedData);
        log.info("Encrypted: {}", Base64.getEncoder().encodeToString(encryptedData));

        // Kết hợp IV + encrypted data
        ByteBuffer byteBuffer = ByteBuffer.allocate(iv.length + encryptedData.length);
        byteBuffer.put(iv);
        byteBuffer.put(encryptedData);
        log.info("ByteBuffer: {}", Arrays.toString(byteBuffer.array()));

        log.info("Base64 encode: {}", Base64.getEncoder().encodeToString(byteBuffer.array()));
        return Base64.getEncoder().encodeToString(byteBuffer.array());
    }

    /**
     * Giải mã văn bản
     */
    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        if (secretKey == null) {
            init();
        }

        log.info("Encrypted: {}", encryptedText);

        byte[] decodedData = Base64.getDecoder().decode(encryptedText);

        log.info("Decrypted: {}", Base64.getEncoder().encodeToString(decodedData));

        // Tách IV và encrypted data
        ByteBuffer byteBuffer = ByteBuffer.wrap(decodedData);
        byte[] iv = new byte[GCM_IV_LENGTH];
        byteBuffer.get(iv);

        log.info("IV: {}", iv);

        byte[] encryptedBytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedBytes);

        log.info("Encrypted: {}", Base64.getEncoder().encodeToString(encryptedBytes));

        // Giải mã
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        log.info("Cipher: {}", cipher);

        byte[] decryptedData = cipher.doFinal(encryptedBytes);
        log.info("Decrypted: {}", Base64.getEncoder().encodeToString(decryptedData));
        log.info("String decryptedData: {}", new String(decryptedData, "UTF-8"));
        return new String(decryptedData, "UTF-8");
    }
}
