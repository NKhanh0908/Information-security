package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Manage user profiles and information")
public class UserController {
    private final UserService userService;

    @Operation(
            summary = "Get Current User",
            description = "Fetches the current authenticated user's profile information",
            responses = {
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "200",
                            description = "User profile retrieved successfully"
                    ),
                    @io.swagger.v3.oas.annotations.responses.ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized access"
                    )
            }
    )
    @GetMapping
    public ResponseEntity<APIResponse<UserDTO>> getCurrentUser(HttpServletRequest request) {
        UserDTO userDTO = userService.getCurrentUser();
        return ResponseEntity.ok(new APIResponse<>(
                true,
                "User profile retrieved successfully",
                userDTO,
                null,
                request.getRequestURI()
        ));
    }

}
