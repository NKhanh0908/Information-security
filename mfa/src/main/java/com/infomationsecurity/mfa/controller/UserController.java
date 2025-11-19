package com.infomationsecurity.mfa.controller;

import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.request.userDTO.UserUpdateDTO;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.dto.response.UserDTO;
import com.infomationsecurity.mfa.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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

//     Khởi tạo API updateUser theo mẫu "Get MFA settings" bên trong MfaSettingController 
//     Chỉ thay đổi các thông số thông báo để phù hợp hơn
    @PatchMapping()
    @Operation(
        summary = "Update information user",
        description = "Update profile",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = UserUpdateDTO.class))
            ),
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Update successful",
                        content = @Content(schema = @Schema(implementation = UserDTO.class))
                ),
                @ApiResponse(responseCode = "401", description = "User not authenticated"),
                @ApiResponse(responseCode = "404", description = "Update user error")
        }
    )
        /*Người dùng gọi API POST /api/v1/user/updateUser kèm JSON.

          Spring Boot map JSON → UserUpdateDTO.

          Controller gọi userService.update(...).

          Service update dữ liệu trong DB và trả về UserDTO.

          Controller gói kết quả vào APIResponse rồi trả về client với HTTP 200. */
        public ResponseEntity<APIResponse<UserDTO>> updateUser(
                @RequestBody UserUpdateDTO userUpdateDTO, HttpServletRequest request
        ){
                UserDTO updatedUser = userService.update(userUpdateDTO);

                return ResponseEntity.ok(new APIResponse<>(
                        true,
                        "User profile updated successfully",
                        updatedUser,
                        null,
                        request.getRequestURI()
                ));
        }
// Luồng chạy từ Swagger → Controller → Service → Repository → Database
/* Client (Swagger) gửi JSON → UserController.

   UserController gọi userService.update.

   UserServiceImpl gọi accountService.getAccountAuth() để lấy account hiện tại.

   accountService tìm account trong DB qua accountRepository.

   userService tìm User trong DB qua userRepository.

   Service cập nhật field nào có dữ liệu trong UserUpdateDTO.

   Lưu lại vào DB → lấy User mới.

   Map sang UserDTO và trả về Controller.

   Controller gói vào APIResponse rồi trả về cho Client.*/
}
