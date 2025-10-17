package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO.FormRequireNonAuth;
import com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO.FormResetPasswordDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Controller", description = "Manage user accounts and authentication")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Operation(
            summary = "Create Account",
            description = "Creates a new user account in the system",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account creation information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AccountCreateDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201",
                            description = "Account created successfully",
                            content = @Content(schema = @Schema(implementation = AccountDTO.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<AccountDTO>> create(@RequestBody AccountCreateDTO accountCreateDTO, HttpServletRequest request) {
        AccountDTO accountDTO = accountService.signUp(accountCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(
                        true,
                        "Account created successfully",
                        accountDTO,
                        null,
                        request.getRequestURI()));
    }



    @GetMapping
    @Operation(
            summary = "Get Account Authentication",
            description = "Retrieve the authenticated account information",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Account authentication retrieved successfully",
                            content = @Content(schema = @Schema(implementation = AccountDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    public ResponseEntity<APIResponse<AccountDTO>> getAccountAuth(HttpServletRequest request) {
        AccountDTO accountDTO = accountService.getAccountAuth();
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Account authentication retrieved successfully",
                accountDTO,
                null,
                request.getRequestURI()));
    }

    @PostMapping("/require-forgot-password")
    @Operation(
            summary = "Request Forgot Password",
            description = "Sends a password reset request to the user’s registered email address",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address used for password recovery",
                    required = true,
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Password reset request sent successfully",
                            content = @Content(schema = @Schema(implementation = APIResponse.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid email or account not found",
                            content = @Content(schema = @Schema(implementation = APIResponse.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> requiredForgotPassword(
            @RequestBody FormRequireNonAuth formRequireNonAuth,
            HttpServletRequest request
    ) {
        Boolean result = accountService.requiredForgotPassword(formRequireNonAuth);

        if (Boolean.TRUE.equals(result)) {
            return ResponseEntity.ok(
                    new APIResponse<>(
                            true,
                            "Password reset request sent successfully",
                            true,
                            null,
                            request.getRequestURI()
                    )
            );
        } else {
            return ResponseEntity
                    .ok(new APIResponse<>(
                            false,
                            "Invalid email or account not found",
                            false,
                            null,
                            request.getRequestURI()
                    ));
        }
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset Password",
            description = "Resets the user’s password after verifying OTP and email",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Information required to reset password",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FormResetPasswordDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Password reset successfully",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid reset information or operation failed",
                            content = @Content)
            }
    )
    public ResponseEntity<APIResponse<Void>> resetPassword(
            @RequestBody FormResetPasswordDTO formResetPasswordDTO,
            HttpServletRequest request
    ) {
            accountService.resetPassword(formResetPasswordDTO);
            return ResponseEntity.ok(
                    new APIResponse<>(
                            true,
                            "Password reset successfully",
                            null,
                            null,
                            request.getRequestURI()
                    )
            );
    }

}
