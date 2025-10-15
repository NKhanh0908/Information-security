package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.response.TOTPRegistrationDTO;
import com.infomationsecurity.mfa.service.TOTPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/totp")
@RequiredArgsConstructor
@Tag(name = "Totp Controller", description = "Totp verify authentication")
public class TOTPController {
    private final TOTPService totpService;

    @PostMapping("/register")
    @Operation(
            summary = "Register TOTP for a user",
            description = "Initiates TOTP registration and returns registration details",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "TOTP registration initiated successfully",
                            content = @Content(schema = @Schema(implementation = TOTPRegistrationDTO.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<TOTPRegistrationDTO>> registerTOTP(HttpServletRequest request) {
        TOTPRegistrationDTO result = totpService.registerTOTP();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new APIResponse<>(true, "TOTP registration initiated successfully", result, null, request.getRequestURI()));
    }

    @PostMapping("/verify-register-totp")
    @Operation(
            summary = "Verify TOTP code",
            description = "Verify a TOTP code provided by the user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = TOTPVerificationDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "TOTP verification result",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyRegisterTOTP(@RequestBody TOTPVerificationDTO verificationDTO, HttpServletRequest request) {
        Boolean result = totpService.verifyRegisterTOTP(verificationDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse<>(true, "TOTP verification result", result, null, request.getRequestURI()));
    }

    @PostMapping("/verify")
    @Operation(
            summary = "Verify TOTP code",
            description = "Verify a TOTP code provided by the user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = TOTPVerificationDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "TOTP verification result",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyTOTP(@RequestBody TOTPVerificationDTO verificationDTO, HttpServletRequest request) {
        Boolean result = totpService.verifyTOTP(verificationDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse<>(true, "TOTP verification result", result, null, request.getRequestURI()));
    }

}
