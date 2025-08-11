package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDTO entityToDTO(User user){
        return UserDTO.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userGender(user.getUserGender().name())
                .userDateOfBirth(user.getUserDateOfBirth() != null ? user.getUserDateOfBirth() : null)
                .userAddress(user.getUserAddress() != null ? user.getUserAddress() : null)
                .userPhone(user.getUserPhone() != null ? user.getUserPhone() : null)
                .userCreatedAt(user.getUserCreatedAt())
                .userUpdatedAt(user.getUserUpdatedAt() != null ? user.getUserUpdatedAt() : null)
                .build();
    }
}
