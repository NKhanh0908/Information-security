package com.infomationsecurity.mfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDTO {
    private Integer userId;
    private String userName;
    private String userGender;
    private String userDateOfBirth;
    private String userAddress;
    private String userPhone;
    private String userCreatedAt;
    private String userUpdatedAt;
}
