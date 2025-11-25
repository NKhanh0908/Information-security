package com.infomationsecurity.mfa;

import com.infomationsecurity.mfa.util.encrypt.RSAEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Test implements CommandLineRunner {
    @Autowired
    private RSAEncryptionService rsaEncryptionService;

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        String text = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";
        String encrypted = rsaEncryptionService.encryptWithPublicKey(text);
        System.out.println("Encrypted: " + encrypted);
        String decrypted = rsaEncryptionService.decryptWithPrivateKey(encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}
