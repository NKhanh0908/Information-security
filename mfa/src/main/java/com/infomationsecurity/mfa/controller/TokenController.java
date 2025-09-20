package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/tokens")
@RequiredArgsConstructor
@Tag(name = "Token Controller", description = "Token management operations")
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh-token")
    @Operation(
            summary = "Refresh Token",
            description = "Refresh the authentication token using a valid refresh token",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token information",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Token refreshed successfully",
                            content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            }
    )
    public ResponseEntity<APIResponse<AuthenticationDTO>> refreshToken(@RequestBody RefreshTokenDTO refreshTokenDTO, HttpServletRequest request) {
        AuthenticationDTO authDTO = tokenService.refreshToken(refreshTokenDTO);
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Token refreshed successfully",
                authDTO,
                null,
                request.getRequestURI()));
    }
}
