package com.infomationsecurity.mfa.service;

import org.springframework.stereotype.Service;

import com.google.zxing.qrcode.decoder.Version.ECB;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@Service
public class RSAService {


    /*
    @Service bean quản lý RSA ops.
    RSA_ALGORITHM chọn RSA OAEP với SHA-256 (an toàn hơn PKCS#1 v1.5). ECB phần lớn là biểu diễn provider; dùng OAEP thực tế.
    publicKey và privateKey lưu cặp khóa tải từ file PEM.
    */ 

    private static final String RSA_ALGORITHM = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() { // init() tự động tải các file PEM từ src/main/resources/keys/.
        try { // Kiểm tra tồn tại file, nếu không có thì ném lỗi, ứng dụng sẽ fail-fast.
            // Load từ file (ví dụ trong resources)
            try (InputStream pub = getClass().getResourceAsStream("/keys/public.pem");
                 InputStream priv = getClass().getResourceAsStream("/keys/private.pem")) {

                if (pub == null || priv == null) {
                    throw new RuntimeException("RSA key files not found in resources/keys/");
                }
                // Gọi loadPublicKey() và loadPrivateKey() để parse PEM → PublicKey/PrivateKey Java objects.
                publicKey = loadPublicKey(pub);
                privateKey = loadPrivateKey(priv);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load RSA keys", e);
        }
    }
    private PublicKey loadPublicKey(InputStream inputStream) throws Exception {
        // Đọc toàn bộ nội dung PEM, loại bỏ header/footer và whitespace.
        String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key); // Base64-decode phần thân để lấy DER bytes (X.509 SubjectPublicKeyInfo).

        // X509EncodedKeySpec + KeyFactory để sinh PublicKey Java.
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes); 
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    // Tương tự, đọc PEM private (ở định dạng PKCS#8). Nếu bạn có private key ở định dạng khác 
    // (ví dụ BEGIN RSA PRIVATE KEY = PKCS#1), cần chuyển sang PKCS#8 hoặc parse khác (hoặc dùng BouncyCastle).
    private PrivateKey loadPrivateKey(InputStream inputStream) throws Exception {
        String key = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    // encryptWithPublicKey: dùng public key để mã hoá một chuỗi plaintext (UTF-8). 
    // Kết quả Base64 để dễ truyền (nhưng lưu ý: RSA có giới hạn kích thước input <= keySize - padding_overhead; 
    // chính vì thế, plaintext phải đủ nhỏ — trong trường hợp của bạn plaintext là Base64 của 16 byte → rất nhỏ và OK).
    public String encryptWithPublicKey(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptWithPrivateKey(String base64CipherText) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(base64CipherText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}