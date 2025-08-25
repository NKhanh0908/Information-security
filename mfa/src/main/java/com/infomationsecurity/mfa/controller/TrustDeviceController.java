package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.fiters.TrustDeviceFilter;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.dto.response.deviceDTO.TrustDeviceDTO;
import com.infomationsecurity.mfa.service.TrustDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/trust-devices")
@RequiredArgsConstructor
@Tag(name = "Trust Device Controller", description = "Manage trust devices")
public class TrustDeviceController {
    private final TrustDeviceService trustDeviceService;

    @Operation(
            summary = "Filter trust devices",
            description = "Retrieve a paginated list of trust devices based on filter criteria",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Trust devices retrieved successfully"
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized access"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<APIResponse<PageDTO<TrustDeviceDTO>>> filter(
            @ParameterObject @ModelAttribute TrustDeviceFilter filter,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            HttpServletRequest request) {

        PageDTO<TrustDeviceDTO> resultPage = trustDeviceService.filter(filter, page, size);
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "Filter trust devices successfully",
                resultPage,
                null,
                request.getRequestURI()
        ));
    }

}
