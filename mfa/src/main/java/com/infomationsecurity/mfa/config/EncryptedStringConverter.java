package com.infomationsecurity.mfa.config;


import com.infomationsecurity.mfa.util.encrypt.AESEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static AESEncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(AESEncryptionService service) {
        EncryptedStringConverter.encryptionService = service;
    }

    /**
     * Mã hóa khi lưu vào database
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return attribute;
        }

        try {
            return encryptionService.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Giải mã khi đọc từ database
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return dbData;
        }

        try {
            return encryptionService.decrypt(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }

}
