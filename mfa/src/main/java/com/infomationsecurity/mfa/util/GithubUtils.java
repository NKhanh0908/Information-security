package com.infomationsecurity.mfa.util;

import com.infomationsecurity.mfa.dto.other.GitHubUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class GithubUtils {
    private final RestTemplate restTemplate;

    @Value("${oauth2.github.client-id}")
    private String githubClientId;

    @Value("${oauth2.github.client-secret}")
    private String githubClientSecret;

    public String exchangeGithubCodeForToken(String authorizationCode) {
        log.info("Exchanging GitHub authorization code for access token: {}", authorizationCode);
        String tokenUrl = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/json");

        Map<String, String> body = Map.of(
                "client_id", githubClientId,
                "client_secret", githubClientSecret,
                "code", authorizationCode
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }

        throw new RuntimeException("Failed to exchange GitHub authorization code for token");
    }

    public GitHubUserInfo getGithubUserInfo(String accessToken) {
        String userInfoUrl = "https://api.github.com/user";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> userInfo = response.getBody();

            // Get user email (might be in separate endpoint if private)
            String email = (String) userInfo.get("email");
            if (email == null) {
                email = getGithubUserEmail(accessToken);
            }

            return GitHubUserInfo.builder()
                    .id(((Number) userInfo.get("id")).toString())
                    .login((String) userInfo.get("login"))
                    .email(email)
                    .name((String) userInfo.get("name"))
                    .avatarUrl((String) userInfo.get("avatar_url"))
                    .build();
        }

        throw new RuntimeException("Failed to get GitHub user info");
    }

    public String getGithubUserEmail(String accessToken) {
        String emailUrl = "https://api.github.com/user/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github.v3+json");

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map[]> response = restTemplate.exchange(emailUrl, HttpMethod.GET, request, Map[].class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            for (Map<String, Object> emailInfo : response.getBody()) {
                if (Boolean.TRUE.equals(emailInfo.get("primary"))) {
                    return (String) emailInfo.get("email");
                }
            }
        }

        return null;
    }
}
