package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.FormVerify;
import com.infomationsecurity.mfa.dto.request.accountDTO.PasswordVerify;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.emailOTP.VerifyOTP;
import com.infomationsecurity.mfa.dto.request.oauth2.OAuth2RequestDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Controller", description = "Authentication and authorization operations")
public class AuthenticationController {
        private final AuthenticationService authenticationService;

        @PostMapping("/github")
        @Operation(summary = "Sign In with GitHub", description = "Authenticate user using GitHub OAuth2 and return access information", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "GitHub OAuth2 authorization code", required = true, content = @Content(schema = @Schema(implementation = OAuth2RequestDTO.class))), responses = {
                        @ApiResponse(responseCode = "200", description = "GitHub authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid authorization code"),
                        @ApiResponse(responseCode = "401", description = "Account not found"),
                        @ApiResponse(responseCode = "423", description = "Account locked")
        })
        public ResponseEntity<APIResponse<AuthenticationDTO>> signInWithGithub(
                        @RequestBody OAuth2RequestDTO oauth2Request, HttpServletRequest request) {
                log.info("Received GitHub OAuth2 authorization code: {}", oauth2Request.getAuthorizationCode());
                AuthenticationDTO authDTO = authenticationService.authWithGitHub(oauth2Request.getAuthorizationCode());
                return ResponseEntity.ok(new APIResponse<>(
                                true,
                                "GitHub authentication successful",
                                authDTO,
                                null,
                                request.getRequestURI()));
        }

        @PostMapping("/sign-in")
        @Operation(summary = "Sign In", description = "Authenticate user and return access information", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(schema = @Schema(implementation = FormVerify.class))), responses = {
                        @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<APIResponse<AuthenticationDTO>> signIn(@RequestBody FormVerify formVerify,
                        HttpServletRequest request) {
                AuthenticationDTO authDTO = authenticationService.signIn(formVerify);

                log.info("[Auth Controller]: {}", authDTO.toString());

                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", authDTO.getRefreshToken())
                                .httpOnly(true) // Quan trọng nhất: Chống XSS
                                .secure(false) // Chỉ gửi qua HTTPS. Tham khảo spring profile.
                                .path("/") // Chỉ gửi đến các API xác thực
                                .maxAge(7 * 24 * 60 * 60) // Thời gian sống (7 ngày)
                                .sameSite("Lax") // Chống CSRF
                                .domain("localhost")
                                .build();

                authDTO.setRefreshToken(null);
                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .body(new APIResponse<>(
                                                true,
                                                "Authentication successful",
                                                authDTO,
                                                null,
                                                request.getRequestURI()));
        }

        @PostMapping("/verify-password")
        @Operation(summary = "Verify Password", description = "Verify the user's password for sensitive operations", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(schema = @Schema(implementation = PasswordVerify.class))), responses = {
                        @ApiResponse(responseCode = "200", description = "Password verification result", content = @Content(schema = @Schema(implementation = Boolean.class))),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials")
        })
        public ResponseEntity<APIResponse<Boolean>> verifyPassword(@RequestBody PasswordVerify passwordVerify,
                        HttpServletRequest request) {
                Boolean isValid = authenticationService.verifyPassword(passwordVerify.getPassword());
                return ResponseEntity.ok(new APIResponse<>(
                                true,
                                "Password verification result",
                                isValid,
                                null,
                                request.getRequestURI()));
        }

        @PostMapping("/send-email-notification-verify")
        @Operation(summary = "Send Email Notification for Verification", description = "Sends an email notification to the user for verification purposes", responses = {
                        @ApiResponse(responseCode = "200", description = "Email notification sent successfully", content = @Content(schema = @Schema(implementation = Void.class))),
                        @ApiResponse(responseCode = "400", description = "Failed to send email notification")
        })
        public ResponseEntity<APIResponse<Void>> sendEmailNotificationVerify(HttpServletRequest request) {
                authenticationService.sendEmailNotificationVerify();
                return ResponseEntity.ok(new APIResponse<>(
                                true,
                                "Email notification sent successfully",
                                null,
                                null,
                                request.getRequestURI()));
        }

        @PostMapping("/verify-otp")
        @Operation(summary = "Verify Email for Verification", description = "Veirfy an email notification to the user for verification purposes", responses = {
                @ApiResponse(responseCode = "200", description = "Email notification sent successfully", content = @Content(schema = @Schema(implementation = VerifyOTP.class))),
                @ApiResponse(responseCode = "400", description = "Failed to send email notification")
        })
        public ResponseEntity<APIResponse<Boolean>> verifyOtp(@RequestBody VerifyOTP verifyOTP, HttpServletRequest request) {
            Boolean isVerify = authenticationService.verifyOtp(verifyOTP);
            return ResponseEntity.ok(new APIResponse<>(
                    true,
                    "Email notification verify status",
                    isVerify,
                    null,
                    request.getRequestURI()));
        }

        @PostMapping("/verify-email")
        @Operation(summary = "Verify Email OTP", description = "Verifies the OTP sent to the user's email", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Email and OTP for verification", required = true, content = @Content(schema = @Schema(implementation = EmailVerificationDTO.class))), responses = {
                        @ApiResponse(responseCode = "200", description = "Email OTP verified successfully", content = @Content(schema = @Schema(implementation = Boolean.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid OTP or email", content = @Content)
        })
        public ResponseEntity<APIResponse<Boolean>> verifyEmail(@RequestBody EmailVerificationDTO emailVerificationDTO,
                        HttpServletRequest request) {
                Boolean result = authenticationService.verifyEmail(emailVerificationDTO);
                return ResponseEntity.ok(new APIResponse<>(
                                result,
                                result ? "Email OTP verified successfully" : "Invalid OTP or email",
                                result,
                                null,
                                request.getRequestURI()));
        }

        @PostMapping("/logout")
        @Operation(summary = "Logout", description = "Invalidates the user's session by clearing the refresh token cookie. Requires a valid Access Token in the Authorization header.", responses = {
                        @ApiResponse(responseCode = "200", description = "Logout successful", content = @Content(schema = @Schema(implementation = APIResponse.class))),
                        @ApiResponse(responseCode = "401", description = "User is not authenticated")
        })
        public ResponseEntity<APIResponse<String>> logout(HttpServletRequest request) {
                ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/") // QUAN TRỌNG: Phải khớp chính xác với path khi tạo cookie
                                .maxAge(0) // << ĐIỂM MẤU CHỐT: Set thời gian sống bằng 0 để trình duyệt xóa ngay lập
                                           // tức
                                .sameSite("Strict")
                                .build();

                // Tùy chọn: Bạn có thể thêm logic ở đây để vô hiệu hóa Access Token hiện tại
                // nếu bạn có một danh sách từ chối (token denylist) ở phía server.

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                                .body(new APIResponse<>(
                                                true,
                                                "Logout successful",
                                                "User has been logged out.",
                                                null,
                                                request.getRequestURI()));
        }
}
