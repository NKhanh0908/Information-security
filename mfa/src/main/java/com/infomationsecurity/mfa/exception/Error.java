package com.infomationsecurity.mfa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum Error {
    // Client Error
    NOT_FOUND(404, "Resource not found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(403, "Forbidden", HttpStatus.FORBIDDEN),
    CONFLICT(409, "Conflict", HttpStatus.CONFLICT),
    METHOD_NOT_ALLOWED(405, "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    TOO_MANY_REQUESTS(429, "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    INVALID_ENUM(422, "Invalid enum", HttpStatus.BAD_REQUEST),

    // Server Error
    UNCATEGORIZED_EXCEPTION(9999, "Unclassified error", HttpStatus.INTERNAL_SERVER_ERROR),
    // Database Error
    DATABASE_ACCESS_ERROR(9998, "Database access error", HttpStatus.INTERNAL_SERVER_ERROR),
    DUPLICATE_KEY(9996, "Duplicate key found", HttpStatus.CONFLICT),
    EMPTY_RESULT(9995, "No result found", HttpStatus.NOT_FOUND),
    NON_UNIQUE_RESULT(9994, "Non-unique result found", HttpStatus.CONFLICT),

    // Account-related errors
    ACCOUNT_NOT_FOUND(1001, "Account not found", HttpStatus.NOT_FOUND),
    ACCOUNT_ALREADY_EXISTS(1002, "Account already exists", HttpStatus.CONFLICT),
    ACCOUNT_UNABLE_TO_SAVE(1003, "Unable to save account", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_UNABLE_TO_UPDATE(1004, "Unable to update account", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_UNABLE_TO_DELETE(1005, "Unable to delete account", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_INVALID_USERNAME(1006, "Invalid username", HttpStatus.BAD_REQUEST),
    ACCOUNT_INVALID_PASSWORD(1007, "Invalid password", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED(1008, "Account is locked", HttpStatus.FORBIDDEN),
    ACCOUNT_USERNAME_TO_SHORT(1008, "Username account to short", HttpStatus.BAD_REQUEST),
    ACCOUNT_USERNAME_TO_LONG(1008, "Username account to long", HttpStatus.BAD_REQUEST),
    ACCOUNT_PASSWORD_TO_SHORT(1008, "Password account to short", HttpStatus.BAD_REQUEST),
    ACCOUNT_LOCKED_TEMPORARILY(1009, "Account is temporarily locked due to too many failed login attempts", HttpStatus.FORBIDDEN),
    PASSWORD_RESET_FAILED(1010, "Password reset failed", HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_RESET_INVALID_REQUEST(1011, "Invalid password reset request", HttpStatus.BAD_REQUEST),
    ACCOUNT_EMAIL_ALREADY_EXISTS(1012, "Email already exists", HttpStatus.CONFLICT),
    ACCOUNT_USERNAME_ALREADY_EXISTS(1013, "Username already exists", HttpStatus.CONFLICT),

    // JWT token-related errors
    JWT_INVALID(14001, "Invalid JWT token", HttpStatus.UNAUTHORIZED),
    JWT_EXPIRED(14002, "JWT token expired", HttpStatus.UNAUTHORIZED),
    JWT_MALFORMED(14003, "Malformed JWT token", HttpStatus.UNAUTHORIZED),
    INVALID_REFRESH_TOKEN(14004, "Invalid refresh token", HttpStatus.UNAUTHORIZED),

    // General validation errors
    INVALID_DATE_FORMAT(33001, "Invalid date format", HttpStatus.BAD_REQUEST),
    INVALID_TIME_FORMAT(33002, "Invalid time format", HttpStatus.BAD_REQUEST),
    INVALID_NUMERIC_VALUE(33003, "Invalid numeric value", HttpStatus.BAD_REQUEST),
    INVALID_ENUM_VALUE(33004, "Invalid enum value", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD_MISSING(33005, "Required field is missing", HttpStatus.BAD_REQUEST),
    FIELD_TOO_LONG(33006, "Field value exceeds maximum length", HttpStatus.BAD_REQUEST),
    FIELD_TOO_SHORT(33007, "Field value is below minimum length", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(33008, "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(33009, "Invalid phone number format", HttpStatus.BAD_REQUEST),

    // Business logic errors
    INSUFFICIENT_PRIVILEGES(34001, "Insufficient privileges to perform this action", HttpStatus.FORBIDDEN),
    OPERATION_NOT_PERMITTED(34002, "Operation not permitted in current state", HttpStatus.BAD_REQUEST),
    RESOURCE_IN_USE(34003, "Resource is currently in use and cannot be modified", HttpStatus.CONFLICT),
    DEADLINE_EXCEEDED(34004, "Deadline has been exceeded", HttpStatus.BAD_REQUEST),
    QUOTA_EXCEEDED(34005, "Quota limit exceeded", HttpStatus.BAD_REQUEST),
    WORKFLOW_VIOLATION(34006, "Action violates workflow rules", HttpStatus.BAD_REQUEST),
    DATA_INTEGRITY_VIOLATION(34007, "Data integrity constraint violation", HttpStatus.CONFLICT),

    // OTP related errors
    OTP_NOT_FOUND(39001, "OTP not found", HttpStatus.NOT_FOUND),
    OTP_EXPIRED_OR_INVALID(39002, "OTP expired or invalid", HttpStatus.UNAUTHORIZED),
    OTP_INVALID(39003, "Invalid OTP", HttpStatus.UNAUTHORIZED),
    OTP_ALREADY_USED(39004, "OTP has already been used", HttpStatus.BAD_REQUEST),
    OTP_MAX_ATTEMPTS_EXCEEDED(39005, "Maximum OTP attempts exceeded", HttpStatus.TOO_MANY_REQUESTS),
    OTP_ALREADY_SENT(39006, "OTP has already been sent", HttpStatus.BAD_REQUEST),
    OTP_SEND_FAILED(39007, "Failed to send OTP", HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_REQUIRED(39008, "OTP is required for this operation", HttpStatus.UNAUTHORIZED),

    // MfaSettings related errors
    MFA_SETTINGS_NOT_FOUND(40001, "MFA settings not found", HttpStatus.NOT_FOUND),
    MFA_SETTINGS_UNABLE_TO_SAVE(40002, "Unable to save MFA settings", HttpStatus.INTERNAL_SERVER_ERROR),
    MFA_SETTINGS_UNABLE_TO_UPDATE(40003, "Unable to update MFA settings", HttpStatus.INTERNAL_SERVER_ERROR),
    MFA_SETTINGS_UNABLE_TO_DELETE(40004, "Unable to delete MFA settings", HttpStatus.INTERNAL_SERVER_ERROR),
    MFA_SETTINGS_INVALID(40005, "Invalid MFA settings", HttpStatus.BAD_REQUEST),
    MFA_SETTINGS_ALREADY_EXISTS(40006, "MFA settings already exist", HttpStatus.CONFLICT),
    MFA_METHOD_NOT_SUPPORTED(40007, "MFA method not supported", HttpStatus.BAD_REQUEST),

    // Totp related errors
    TOTP_SECRET_KEY_NOT_FOUND(41001, "TOTP secret key not found", HttpStatus.NOT_FOUND),
    TOTP_REGISTRATION_FAILED(41002, "TOTP registration failed", HttpStatus.INTERNAL_SERVER_ERROR),
    TOTP_VERIFICATION_FAILED(41003, "TOTP verification failed", HttpStatus.UNAUTHORIZED),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    /**
     * Constructor for ErrorCode.
     *
     * @param code       the error code
     * @param message    the error message
     * @param statusCode the corresponding HTTP status code
     */
    Error(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}