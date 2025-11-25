package com.infomationsecurity.mfa.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infomationsecurity.mfa.util.encrypt.AESEncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Converter
@Component
public class BackupCodesConverter implements AttributeConverter<List<String>, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Đổi sang static để inject được
    private static AESEncryptionService encryptionService;

    @Autowired
    public void setEncryptionService(AESEncryptionService service) {
        BackupCodesConverter.encryptionService = service;
    }

    @Override
    public String convertToDatabaseColumn(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return null;
        }

        try {
            // 1) List -> JSON
            String json = objectMapper.writeValueAsString(attribute);

            // 2) JSON -> Encrypt
            return encryptionService.encrypt(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting backup codes to encrypted JSON", e);
        }
    }

    @Override
    public List<String> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }

        try {
            // 1) Decrypt -> JSON
            String json = encryptionService.decrypt(dbData);

            // 2) JSON -> List
            return objectMapper.readValue(
                    json,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Error decrypting backup codes JSON", e);
        }
    }
}