package com.infomationsecurity.mfa.dto.other;

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