package com.infomationsecurity.mfa.exception;

import com.infomationsecurity.mfa.dto.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class CustomizedResponseEntityExceptionHandler {

        // Bắt BadRequestException (nếu bạn vẫn dùng)
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<APIResponse<Object>> handleBadRequest(BadRequestException ex, WebRequest request) {
                APIResponse<Object> body = new APIResponse<>(
                        false,
                        ex.getMessage(),
                        null,
                        Collections.singletonList(ex.getMessage()),
                        request.getDescription(false)
                );
                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // --- Thêm handler cho CustomException ---
        @ExceptionHandler(CustomException.class)
        public ResponseEntity<APIResponse<Object>> handleCustomException(CustomException ex, WebRequest request) {
                // Log lại server-side stacktrace để debug
                log.error("CustomException thrown: {}", ex.getMessage(), ex);

                // Lấy danh sách message chi tiết (có kèm ID nếu có)
                List<String> details = ex.getErrorMessagesWithId();

                APIResponse<Object> body = new APIResponse<>(
                        false,
                        ex.getMessage(),      // trả về message tổng hợp
                        null,
                        details,              // trả về list chi tiết
                        request.getDescription(false)
                );
                // Trả về đúng HTTP status mà CustomException định nghĩa
                return new ResponseEntity<>(body, ex.getStatusCode());
        }

        // Bắt validation errors (@Valid)
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<APIResponse<Object>> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
                List<String> errors = ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(err -> err.getField() + ": " + err.getDefaultMessage())
                        .collect(Collectors.toList());

                APIResponse<Object> body = new APIResponse<>(
                        false,
                        "Validation failed",
                        null,
                        errors,
                        request.getDescription(false)
                );
                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        // Bắt AccessDenied (403)
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<APIResponse<Object>> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
                APIResponse<Object> body = new APIResponse<>(
                        false,
                        "Access denied",
                        null,
                        Collections.singletonList(ex.getMessage()),
                        request.getDescription(false)
                );
                return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
        }

        // Bắt tất cả các exception khác (500)
        @ExceptionHandler(Exception.class)
        public ResponseEntity<APIResponse<Object>> handleAll(Exception ex, WebRequest request) {
                log.error("Unhandled exception: {}", ex.getMessage(), ex);

                APIResponse<Object> body = new APIResponse<>(
                        false,
                        "Internal server error",
                        null,
                        Collections.singletonList(ex.getMessage()),
                        request.getDescription(false)
                );
                return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
        }
}