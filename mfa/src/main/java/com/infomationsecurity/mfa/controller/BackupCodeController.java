package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.service.BackupCodeService;
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

import java.util.List;

@RestController
@RequestMapping("/backup-codes")
@RequiredArgsConstructor
@Tag(name = "Backup Code Controller", description = "Backup code management operations")
public class BackupCodeController {
    private final BackupCodeService backupCodeService;

    @PostMapping("/generate")
    @Operation(
            summary = "Generate Backup Codes",
            description = "Generate a new set of backup codes for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Backup codes generated successfully"
                    )
            }
    )
    public ResponseEntity<APIResponse<Void>> generateBackupCodes(HttpServletRequest request) {
        backupCodeService.generateBackupCodes();
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new APIResponse<>(true, "Backup codes generated successfully", null, null, request.getRequestURI())
        );
    }

    @GetMapping
    @Operation(
            summary = "Get Backup Codes",
            description = "Retrieve the list of active backup codes for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Backup codes retrieved successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<List<String>>> getBackupCodes(HttpServletRequest request) {
        List<String> codes = backupCodeService.getBackupCodes();
        return ResponseEntity.ok(
                new APIResponse<>(true, "Backup codes retrieved successfully", codes, null, request.getRequestURI())
        );
    }

    @PostMapping("/verify")
    @Operation(
            summary = "Verify Backup Code",
            description = "Verify and invalidate a specific backup code for the authenticated user",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = String.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Backup code verification result",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> verifyBackupCode(@RequestBody String backupCode,
                                                                 HttpServletRequest request) {
        Boolean result = backupCodeService.invalidateBackupCode(backupCode);
        return ResponseEntity.ok(
                new APIResponse<>(true, "Backup code verification result", result, null, request.getRequestURI())
        );
    }

    @DeleteMapping
    @Operation(
            summary = "Delete Backup Codes",
            description = "Delete backup codes for the authenticated user",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Backup codes Deleted successfully"
                    )
            }
    )
    public ResponseEntity<APIResponse<Void>> deleteBackupCode(HttpServletRequest request) {
        backupCodeService.deleteBackupCodes();
        return ResponseEntity.status(200).body(
                new APIResponse<>(true, "Backup codes Deleted successfully", null, null, request.getRequestURI())
        );
    }

}
