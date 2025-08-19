package com.infomationsecurity.mfa.dto.request.userDTO;

import com.infomationsecurity.mfa.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserUpdateDTO {
    private String userName;

    private Gender userGender;

    private LocalDate userDateOfBirth;

    private String userAddress;

    private String userPhone;
}
