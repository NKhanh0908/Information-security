package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mfa-settings")
@RequiredArgsConstructor
@Tag(name = "Login attempt Controller", description = "List and manage login attempts")
public class MfaSettingsController {
    private final MfaSettingsService mfaSettingsService;

    @GetMapping
    @Operation(
            summary = "Get MFA settings",
            description = "Retrieve MFA settings for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "MFA settings retrieved successfully",
                            content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Account not found"),
                    @ApiResponse(responseCode = "404", description = "MFA settings not found")
            }
    )
    public ResponseEntity<APIResponse<MfaSettingsDTO>> getMfaSettings(HttpServletRequest request) {
        MfaSettingsDTO mfaSettingsDTO = mfaSettingsService.getMfaSetting();

        return ResponseEntity.ok(new APIResponse<>(
                true,
                "MFA settings retrieved successfully",
                mfaSettingsDTO,
                null,
                request.getRequestURI()
        ));
    }

    @PostMapping("/verify-totp")
    @Operation(
            summary = "Verify login with TOTP",
            description = "Verify login attempt by validating TOTP code for trusted device",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "TOTP verification request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyDeviceWithTOTP.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "TOTP verification successful",
                            content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid TOTP code"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized - Account not found"),
                    @ApiResponse(responseCode = "423", description = "Account locked")
            }
    )
    public ResponseEntity<APIResponse<AuthenticationDTO>> verifyLoginWithTOTP(
            @RequestBody VerifyDeviceWithTOTP verifyDeviceWithTOTP,
            HttpServletRequest request
    ) {
        AuthenticationDTO authDTO = mfaSettingsService.verifyLoginWithTOTP(verifyDeviceWithTOTP);

        return ResponseEntity.ok(new APIResponse<>(
                true,
                "TOTP verification successful",
                authDTO,
                null,
                request.getRequestURI()
        ));
    }
}
