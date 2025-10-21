package com.infomationsecurity.mfa.controller.nonAuth;

import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO.FormRequireNonAuth;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.VerificationResult;
import com.infomationsecurity.mfa.service.MailService;
import com.infomationsecurity.mfa.service.nonAuth.NAMailService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/na-mail")
@RequiredArgsConstructor
@Tag(name = "Mail Controller un Auth", description = "Manage email verification and OTP operations")
public class NAMailController {
    private final NAMailService naMailService;

    @PostMapping("/require-auth")
    @Operation(
            summary = "Send Forgot Password Email",
            description = "Sends an email with OTP to the user for password recovery",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email information for password recovery",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FormRequireNonAuth.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Password recovery email sent successfully",
                            content = @Content(schema = @Schema(implementation = Void.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Failed to send password recovery email",
                            content = @Content)
            }
    )
    public ResponseEntity<APIResponse<Void>> sendEmailRequiredForgotPassword(
            @RequestBody FormRequireNonAuth formRequireNonAuth,
            HttpServletRequest request
    ) {
            naMailService.sendEmailRequiredForgotPassword(formRequireNonAuth);
            return ResponseEntity.ok(
                    new APIResponse<>(
                            true,
                            "Password recovery email sent successfully",
                            null,
                            null,
                            request.getRequestURI()
                    )
            );
        }

    @PostMapping("/verify-auth")
    @Operation(
            summary = "Verify Email OTP",
            description = "Verifies the OTP sent to the user's email",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and OTP for verification",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EmailVerificationDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Email OTP verified successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid OTP or email",
                            content = @Content)
            }
    )
    public ResponseEntity<APIResponse<VerificationResult>> verifyEmailRequiredForgotPassword(
            @RequestBody EmailVerificationDTO emailVerificationDTO,
            HttpServletRequest request
    ) {
        VerificationResult result = naMailService.verifyEmailRequiredForgotPassword(emailVerificationDTO);
        return ResponseEntity.ok(
                new APIResponse<>(
                        true,
                        result.getSuccess() ? "Email OTP verified successfully" : "Invalid OTP or email",
                        result,
                        null,
                        request.getRequestURI()
                )
        );
    }
}
