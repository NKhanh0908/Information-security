package com.infomationsecurity.mfa.controller.nonAuth;

import com.infomationsecurity.mfa.dto.request.nonAuth.totpDTO.TOTPVerificationAuth;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.service.nonAuth.NATOTPService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/na-totp")
@RequiredArgsConstructor
@Tag(name = "Totp Controller", description = "Totp verify authentication")
public class NATOTPController {
    private final NATOTPService natotpService;

    @PostMapping("/verify")
    @Operation(
            summary = "Verify TOTP Code",
            description = "Verifies a Time-based One-Time Password (TOTP) used for authentication",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "TOTP verification request containing username/email and the OTP code",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TOTPVerificationAuth.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "TOTP verified successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or expired TOTP code",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyTOTP(
            @RequestBody TOTPVerificationAuth totpVerificationAuth,
            HttpServletRequest request
    ) {
        Boolean result = natotpService.verifyTOTP(totpVerificationAuth);
        return ResponseEntity.ok(
                new APIResponse<>(
                        result,
                        result ? "TOTP verified successfully" : "Invalid or expired TOTP code",
                        result,
                        null,
                        request.getRequestURI()
                )
        );
    }
}
