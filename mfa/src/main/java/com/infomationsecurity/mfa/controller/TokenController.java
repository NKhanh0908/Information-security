package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/tokens")
@RequiredArgsConstructor
@Tag(name = "Token Controller", description = "Token management operations")
public class TokenController {
    private final TokenService tokenService;

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh Access Token", description = "Generates a new access token using the 'refreshToken' sent in an HttpOnly cookie. The request body should be empty.",
            // 1. Xóa @RequestBody và thay bằng @Parameter để mô tả cookie
            parameters = {
                    @Parameter(name = "refreshToken", in = ParameterIn.COOKIE, description = "The refresh token stored in an HttpOnly cookie.", required = true, schema = @Schema(type = "string"))
            }, responses = {
                    @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            })
    public ResponseEntity<APIResponse<AuthenticationDTO>> refreshToken(
            @CookieValue(name = "refreshToken") String refreshTokenString, HttpServletRequest request) {

        try {
            if (refreshTokenString == null || refreshTokenString.isEmpty()) {
                throw new IllegalArgumentException("Refresh token is missing");
            }

            // Check black list

            AuthenticationDTO authDTO = tokenService.refreshToken(refreshTokenString);

            ResponseCookie newRefreshTokenCookie = ResponseCookie
                    .from("refreshToken", authDTO.getRefreshToken())
                    .httpOnly(true) // Quan trọng nhất: Chống XSS
                    .secure(false) // Chỉ gửi qua HTTPS. Tham khảo spring profile.
                    .path("/") // Chỉ gửi đến các API xác thực
                    .maxAge(7 * 24 * 60 * 60) // Thời gian sống (7 ngày)
                    .sameSite("Lax") // Chống CSRF
                    .domain("localhost")
                    .build();

            authDTO.setRefreshToken(null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, newRefreshTokenCookie.toString())
                    .body(new APIResponse<>(
                            true,
                            "Token refreshed successfully",
                            authDTO,
                            null,
                            request.getRequestURI()));
        } catch (Exception e) {
            // Xử lý các lỗi và trả về 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new APIResponse<>(
                    false,
                    "Invalid or expired refresh token.",
                    null,
                    List.of(e.getMessage()),
                    request.getRequestURI()));
        }
    }
}
