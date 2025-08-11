package com.infomationsecurity.mfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserDTO {
    private Integer userId;
    private String userName;
    private String userGender;
    private LocalDate userDateOfBirth;
    private String userAddress;
    private String userPhone;
    private LocalDateTime userCreatedAt;
    private LocalDateTime userUpdatedAt;
}
