package com.infomationsecurity.mfa.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class APIResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private String timestamp;
    private String path;

    public APIResponse() {
        this.timestamp = Instant.now().toString();
    }

    public APIResponse(boolean success, String message, T data, List<String> errors, String path) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = errors;
        this.timestamp = Instant.now().toString();
        this.path = path;
    }
}
