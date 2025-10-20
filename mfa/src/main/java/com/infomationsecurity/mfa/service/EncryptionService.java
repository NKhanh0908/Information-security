package com.infomationsecurity.mfa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct; // gói javax.annotation
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

import com.infomationsecurity.mfa.service.RSAService;

@Service
public class EncryptionService {

    private static final String AES = "AES"; // Hằng AES là tên thuật toán dùng khi tạo SecretKeySpec.
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding"; // AES_GCM_NO_PADDING là tên transformation cho Cipher.getInstance(...) — GCM là chế độ mã hoá kèm xác thực (AEAD).
    private static final int GCM_TAG_LENGTH = 128; // độ dài tag xác thực (thường 128-bit)
    private static final int IV_SIZE = 12; // kích thước nonce/IV khuyến nghị cho GCM (12 bytes = 96 bits).

    @Value("${app.encryption.key}") // Value để lấy cấu hình từ application.properties.
    private String base64Key; // chuỗi Base64 = qTmXPNmA6BecHDQ5Mt9QEA==

    private SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();  // SecureRandom để sinh IV ngẫu nhiên an toàn.   


    @Autowired
    private RSAService rsaService;

    @PostConstruct
    public void init() {
        try {
            // Ở đây base64Key (cấu hình app.encryption.key) là giá trị AES key đã bị RSA mã hóa và Base64 
            // (tức là kết quả encryptWithPublicKey(aesKeyBase64)).
            String decryptedKey = rsaService.decryptWithPrivateKey(base64Key); // rsaService.decryptWithPrivateKey(base64Key) sẽ trả về chuỗi Base64 gốc của AES key (ví dụ qTmXPNmA6BecHDQ5Mt9QEA==).
            
            // Sau đó mới Base64.decode để lấy 16 bytes thực sự, rồi tạo SecretKeySpec.
            byte[] keyBytes = Base64.getDecoder().decode(decryptedKey); // Giải Base64 base64Key → keyBytes.
            if (keyBytes.length != 16) { // Kiểm tra chiều dài == 16 bytes (AES-128). Nếu không đúng ném lỗi
                throw new IllegalArgumentException("Encryption key must be 16 bytes for AES-128");
            }
            secretKey = new SecretKeySpec(keyBytes, AES);  // Tạo SecretKeySpec từ keyBytes với thuật toán "AES".
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize EncryptionService: " + ex.getMessage(), ex);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null) return null; // Nếu plainText null ⇒ trả null
        try {
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv); // Sinh iv (nonce) 12 bytes ngẫu nhiên qua secureRandom.

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv); // GCMParameterSpec cần tag length (bit) + iv.
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);  // Cipher.getInstance("AES/GCM/NoPadding") tạo cipher GCM.
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec); // KHÔNG truyền AAD (additional authenticated data) ở đây; nếu cần bạn có thể cipher.updateAAD(...).

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));  // trả về ciphertext || tag

            byte[] combined = new byte[iv.length + cipherText.length];  // Để truyền IV cùng ciphertext, bạn nối iv || ciphertext (lưu ý thứ tự quan trọng).
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(combined);  // Mã hoá kết quả bằng Base64 để dễ lưu/truyền (ví dụ lưu vào DB hoặc JSON).
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    public String decrypt(String base64IvAndCipherText) {
        if (base64IvAndCipherText == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(base64IvAndCipherText);  // Giải mã chuỗi Base64 nhận vào ⇒ combined = iv || ciphertext.

            if (combined.length < IV_SIZE) { // Kiểm tra độ dài tối thiểu.
                throw new IllegalArgumentException("Invalid encrypted data");
            }

            // Tách iv và ciphertext.
            byte[] iv = new byte[IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            int cipherTextLen = combined.length - IV_SIZE;  // GCMParameterSpec với cùng tag length + iv.
            byte[] cipherText = new byte[cipherTextLen];
            System.arraycopy(combined, IV_SIZE, cipherText, 0, cipherTextLen);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

            // nếu tag bị sửa đổi hoặc dữ liệu bị thay đổi, doFinal sẽ ném AEADBadTagException.
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec); 
            byte[] plainBytes = cipher.doFinal(cipherText);
            return new String(plainBytes, "UTF-8");  // Trả về plaintext UTF-8.
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
