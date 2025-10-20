package com.infomationsecurity.mfa;

import com.infomationsecurity.mfa.service.RSAService;

public class EncryptAesKey {
     public static void main(String[] args) throws Exception {
        RSAService rsaService = new RSAService();
        rsaService.init(); // Load public/private key từ resources/keys/

        // AES key hiện tại (base64)
        String aesKeyBase64 = "qTmXPNmA6BecHDQ5Mt9QEA==";

        // Mã hóa AES key bằng RSA public key
        String encryptedKey = rsaService.encryptWithPublicKey(aesKeyBase64);

        System.out.println("🔐 Encrypted AES key:");
        System.out.println(encryptedKey);
    }
}
