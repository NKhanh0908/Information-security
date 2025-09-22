package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailResendOTP;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDevice;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.service.MailService;
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
@RequestMapping("/mail")
@RequiredArgsConstructor
@Tag(name = "Mail Controller", description = "Manage email verification and OTP operations")
public class MailController {

    private final MailService mailService;

    @PostMapping("/send-verification")
    @Operation(
            summary = "Send Verification OTP Email",
            description = "Sends an OTP verification email to the specified account",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address to send OTP",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EmailResendOTP.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "202",
                            description = "Verification email sent successfully",
                            content = @Content
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid email or rate limit exceeded",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Void>> sendVerificationEmail(@RequestBody EmailResendOTP email, HttpServletRequest request) {
        try {
            mailService.sendVerificationOTPEmail(email);
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new APIResponse<>(
                            true,
                            "Verification email sent successfully",
                            null,
                            null,
                            request.getRequestURI()));
        } catch (RuntimeException e) {
            log.error("Error sending verification email to {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new APIResponse<>(
                            false,
                            e.getMessage(),
                            null,
                            null,
                            request.getRequestURI()));
        }
    }

    @PostMapping("/verify")
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
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyEmail(@RequestBody EmailVerificationDTO emailVerificationDTO, HttpServletRequest request) {
        Boolean result = mailService.verifyEmail(emailVerificationDTO);
        return ResponseEntity.ok(new APIResponse<>(
                result,
                result ? "Email OTP verified successfully" : "Invalid OTP or email",
                result,
                null,
                request.getRequestURI()));
    }

    @PostMapping("/verified-signup")
    @Operation(
            summary = "Complete Verified Signup",
            description = "Verifies OTP and completes signup with MFA settings, trust device creation, and sends welcome email",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and OTP for signup verification",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EmailVerificationDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Signup verification completed successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid OTP or email",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifiedSignUp(@RequestBody EmailVerificationDTO emailVerificationDTO, HttpServletRequest request) {
        Boolean result = mailService.verifiedSignUp(emailVerificationDTO);
        return ResponseEntity.ok(new APIResponse<>(
                result,
                result ? "Signup verification completed successfully" : "Invalid OTP or email",
                result,
                null,
                request.getRequestURI()));
    }

    @PostMapping("/send-email-device")
    @Operation(
            summary = "Verify Device via Email",
            description = "Sends verification email for device trust setup and returns result",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Form containing email and device information for verification",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FormVerify.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Verification email sent successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Failed to send verification email",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Void>> sendEmailVerifyDevice(
            @RequestBody FormVerify formVerify,
            HttpServletRequest request
    ) {
            mailService.sendEmailVerifyDevice(formVerify);
            return ResponseEntity.ok(new APIResponse<>(
                    true,
                    "Verification email sent successfully",
                    null,
                    null,
                    request.getRequestURI()
            ));
        }

    @PostMapping("/verify-email-device")
    @Operation(
            summary = "Verify Email Device",
            description = "Verifies the device trust using email confirmation (OTP or token)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and verification token for device trust",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EmailVerificationDevice.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Device verified successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "400",
                            description = "Invalid email or verification token",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyEmailDevice(
            @RequestBody EmailVerificationDevice emailVerificationDevice,
            HttpServletRequest request
    ) {
        Boolean result = mailService.verifyEmailDevice(emailVerificationDevice);

        return ResponseEntity.ok(new APIResponse<>(
                result,
                result ? "Device verified successfully" : "Invalid email or verification token",
                result,
                null,
                request.getRequestURI()
        ));
    }


}



