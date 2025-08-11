package com.infomationsecurity.mfa.dto.request.accountDTO;

import com.infomationsecurity.mfa.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccountCreateDTO {
    @Schema(description = "Full name of the account holder",
            example = "Nguyen Van A",
            maxLength = 100,
            nullable = false)
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must be at most 100 characters")
    private String fullName;

    @Schema(description = "Gender of the account holder",
            example = "MALE",
            nullable = true,
            implementation = Gender.class)
    private Gender gender;

    @Schema(description = "Username for login",
            example = "nguyenvana",
            minLength = 4,
            maxLength = 20,
            nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_.-]+$",
            message = "Username can only contain letters, numbers, dots, underscores, and hyphens")
    private String username;

    @Schema(description = "Password for login",
            example = "P@ssw0rd",
            minLength = 5,
            nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 5, message = "Password must be at least 8 characters")
    // @Pattern(...) nếu muốn enforce password complexity
    private String password;

    @Schema(description = "Email address",
            example = "user@example.com",
            nullable = false)
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
