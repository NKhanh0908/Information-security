package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.request.oauth2.OAuth2RequestDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
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

    @PostMapping("/auth/github")
    @Operation(
            summary = "Sign In with GitHub",
            description = "Authenticate user using GitHub OAuth2 and return access information",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "GitHub OAuth2 authorization code",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OAuth2RequestDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "GitHub authentication successful",
                            content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Invalid authorization code"),
                    @ApiResponse(responseCode = "401", description = "Account not found"),
                    @ApiResponse(responseCode = "423", description = "Account locked")
            }
    )
    public ResponseEntity<APIResponse<AuthenticationDTO>> signInWithGithub(@RequestBody OAuth2RequestDTO oauth2Request, HttpServletRequest request) {
        log.info("Received GitHub OAuth2 authorization code: {}", oauth2Request.getAuthorizationCode());
        AuthenticationDTO authDTO = accountService.authWithGitHub(oauth2Request.getAuthorizationCode());
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "GitHub authentication successful",
                authDTO,
                null,
                request.getRequestURI()));
    }

    @PostMapping("/sign-in")
    @Operation(
            summary = "Sign In",
            description = "Authenticate user and return access information",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FormLoginDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Authentication successful",
                            content = @Content(schema = @Schema(implementation = AuthenticationDTO.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<APIResponse<AuthenticationDTO>> signIn(@RequestBody FormLoginDTO formLoginDTO, HttpServletRequest request) {
        AuthenticationDTO authDTO = accountService.signIn(formLoginDTO);
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Authentication successful",
                authDTO,
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
        AuthenticationDTO authDTO = accountService.refreshToken(refreshTokenDTO);
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Token refreshed successfully",
                authDTO,
                null,
                request.getRequestURI()));
    }

}
