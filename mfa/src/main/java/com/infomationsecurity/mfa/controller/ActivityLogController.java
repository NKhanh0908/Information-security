package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.fiters.ActivityLogFilter;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/activity-logs")
@RequiredArgsConstructor
@Tag(name = "Activity log Controller", description = "Manage activity logs for user actions")
public class ActivityLogController {
    private final ActivityLogService activityLogService;

    @GetMapping("/filter")
    @Operation(
            summary = "Filter activity logs",
            description = "Filter activity logs based on various criteria",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Activity logs filtered successfully",
                            content = @Content(schema = @Schema(implementation = ActivityLogDTO.class))
                    )
            }
    )
    public ResponseEntity<APIResponse<PageDTO<ActivityLogDTO>>> filterActivityLogs(
            @ParameterObject @ModelAttribute ActivityLogFilter filter,
            @Parameter(description = "Page number for pagination") Integer page,
            @Parameter(description = "Page size for pagination") Integer size,
            HttpServletRequest request) {

        PageDTO<ActivityLogDTO> result = activityLogService.getActivityLogs(filter, page, size);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new APIResponse<>(
                        true,
                        "Activity logs filtered successfully",
                        result,
                        null,
                        request.getRequestURI()
                ));
    }


}
