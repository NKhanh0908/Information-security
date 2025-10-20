package com.infomationsecurity.mfa;

import com.infomationsecurity.mfa.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Test implements CommandLineRunner {

    @Autowired
    private EncryptionService encryptionService;

    public static void main(String[] args) {
        SpringApplication.run(Test.class, args);
    }

    @Override
    public void run(String... args) {
        String text = "hello world";
        String encrypted = encryptionService.encrypt(text);
        System.out.println("Encrypted: " + encrypted);
        String decrypted = encryptionService.decrypt(encrypted);
        System.out.println("Decrypted: " + decrypted);
    }
}
