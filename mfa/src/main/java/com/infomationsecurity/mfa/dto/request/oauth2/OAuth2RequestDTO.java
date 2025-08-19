package com.infomationsecurity.mfa.dto.request.oauth2;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "OAuth2 authorization request containing the authorization code")
public class OAuth2RequestDTO {

    @NotBlank(message = "Authorization code is required")
    @Schema(description = "OAuth2 authorization code received from the provider",
            example = "4/0AfgeXvs...")
    private String authorizationCode;

    @Schema(description = "Redirect URI used in the OAuth2 flow",
            example = "http://localhost:8080/oauth2/callback/google")
    private String redirectUri;
}