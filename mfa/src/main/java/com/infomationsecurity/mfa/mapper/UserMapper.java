package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.entity.User;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper {
    public UserDTO entityToDTO(User user){
        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userGender(user.getUserGender() != null ? user.getUserGender() : null)
                .userDateOfBirth(user.getUserDateOfBirth() != null ? LocalDate.parse(user.getUserDateOfBirth()) : null)
                .userAddress(user.getUserAddress() != null ? user.getUserAddress() : null)
                .userPhone(user.getUserPhone() != null ? user.getUserPhone() : null)
                .userCreatedAt(user.getUserCreatedAt())
                .userUpdatedAt(user.getUserUpdatedAt() != null ? user.getUserUpdatedAt() : null)
                .build();
    }
}
