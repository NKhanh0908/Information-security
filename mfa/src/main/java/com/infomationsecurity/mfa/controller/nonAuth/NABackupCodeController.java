package com.infomationsecurity.mfa.controller.nonAuth;

import com.infomationsecurity.mfa.dto.request.nonAuth.backupCodeDTO.BackupCodeVerificationAuth;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.service.nonAuth.NABackupCodeService;
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
@RequestMapping("/na-backup-codes")
@RequiredArgsConstructor
@Tag(name = "Backup Code non-Auth Controller", description = "Backup code management operations")
public class NABackupCodeController {
    private final NABackupCodeService backupCodeService;

    @PostMapping("/invalidate")
    @Operation(
            summary = "Invalidate Backup Code",
            description = "Invalidates a backup code after it has been used for authentication",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Backup code information used for verification and invalidation",
                    required = true,
                    content = @Content(schema = @Schema(implementation = BackupCodeVerificationAuth.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Backup code invalidated successfully",
                            content = @Content(schema = @Schema(implementation = Boolean.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid or already used backup code",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<APIResponse<Boolean>> invalidateBackupCode(
            @RequestBody BackupCodeVerificationAuth backupCodeVerificationAuth,
            HttpServletRequest request
    ) {
        Boolean result = backupCodeService.invalidateBackupCode(backupCodeVerificationAuth);

        return ResponseEntity.ok(
                new APIResponse<>(
                        result,
                        result ? "Backup code invalidated successfully" : "Invalid or already used backup code",
                        result,
                        null,
                        request.getRequestURI()
                )
        );
    }
}
