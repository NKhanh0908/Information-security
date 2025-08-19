package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.infomationsecurity.mfa.service.LoginAttemptService;

import java.util.List;

@RestController
@RequestMapping("/login-attempts")
@RequiredArgsConstructor
@Tag(name = "Login attempt Controller", description = "List and manage login attempts")
public class LoginAttemptController {
    private final LoginAttemptService loginAttemptService;

    @GetMapping
    @Operation(
            summary = "Get login attempts by account",
            description = "Retrieve a list of login attempts associated with the current account",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login attempts retrieved successfully",
                            content = @Content(schema = @Schema(implementation = LoginAttemptDTO.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<List<LoginAttemptDTO>>> getLoginAttemptByAccount(HttpServletRequest request) {
        List<LoginAttemptDTO> result = loginAttemptService.getLoginAttemptByAccount();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse<>(true, "Login attempts retrieved successfully", result, null, request.getRequestURI()));
    }

    @GetMapping("/trust-device/{trustDeviceId}")
    @Operation(
            summary = "Get login attempts by trust device ID",
            description = "Retrieve a list of login attempts associated with the specified trust device ID",
            parameters = {
                    @Parameter(name = "trustDeviceId", description = "ID of the trusted device", required = true)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Login attempts retrieved successfully",
                            content = @Content(schema = @Schema(implementation = LoginAttemptDTO.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<List<LoginAttemptDTO>>> getLoginAttemptByTrustDeviceId(@PathVariable Integer trustDeviceId, HttpServletRequest request) {
        List<LoginAttemptDTO> result = loginAttemptService.getLoginAttemptByTrustDeviceId(trustDeviceId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse<>(true, "Login attempts retrieved successfully", result, null, request.getRequestURI()));
    }

}
