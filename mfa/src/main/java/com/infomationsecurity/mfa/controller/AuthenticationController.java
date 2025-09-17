package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
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
        AuthenticationDTO authDTO = authenticationService.authWithGitHub(oauth2Request.getAuthorizationCode());
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
        AuthenticationDTO authDTO = authenticationService.signIn(formLoginDTO);
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Authentication successful",
                authDTO,
                null,
                request.getRequestURI()));
    }

    @GetMapping("/verify-password")
    @Operation(
            summary = "Verify Password",
            description = "Verify the user's password for sensitive operations",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = FormLoginDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "Password verification result",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyPassword(@RequestParam FormLoginDTO formLoginDTO, HttpServletRequest request) {
        Boolean isValid = authenticationService.verifyPassword(formLoginDTO.getPassword());
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Password verification result",
                isValid,
                null,
                request.getRequestURI()));
    }
}
