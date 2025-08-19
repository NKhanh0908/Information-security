package com.infomationsecurity.mfa.dto.request.userDTO;

import com.infomationsecurity.mfa.enums.Gender;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
public class UserCreateDTO {

    @NotBlank(message = "Username is required")
    private String userName;

    @NotNull(message = "Gender is required")
    private Gender userGender;

    @NotBlank(message = "Date of birth is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date of birth must be in format yyyy-MM-dd")
    private LocalDate userDateOfBirth;

    @NotBlank(message = "Address is required")
    private String userAddress;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^(\\+?84|0)(\\d{9})$", message = "Invalid phone number")
    private String userPhone;
}
