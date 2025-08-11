package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.APIResponse;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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

}
