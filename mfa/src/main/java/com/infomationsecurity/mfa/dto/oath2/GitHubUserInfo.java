package com.infomationsecurity.mfa.dto.oath2;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GitHubUserInfo {
    private String id;
    private String login;
    private String email;
    private String name;
    private String avatarUrl;
}