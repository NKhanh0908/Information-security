package com.infomationsecurity.mfa.util;


import com.infomationsecurity.mfa.util.encrypt.RSAEncryptionService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtTokenUtil {

    private SecretKey secretKey;

    @Value("${jwt.secret-key}")
    private String jwtSecretKey;

    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    @Autowired
    private RSAEncryptionService rsaService;

    @PostConstruct
    public void init() throws Exception {
        String decryptedKey = rsaService.decryptWithPrivateKey(jwtSecretKey);
        byte[] keyBytes = Base64.getDecoder().decode(decryptedKey.getBytes(StandardCharsets.UTF_8));
        this.secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public JwtTokenUtil(
            @Value("${jwt.access-expiration:900000}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpiration
    ) {
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;

    }

    private Map<String, Object> extractRole(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        claims.put("roles", roles);
        return claims;
    }

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extractRole(userDetails))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken( UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extractRole(userDetails))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(
                Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
        );
    }



    public String extractTokenGetUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public boolean isTokenExpired( String token) {
        return extractClaims( token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractTokenGetUsername(token);
        if (!username.equals(userDetails.getUsername())) {
            return false;
        }
        if (isTokenExpired(token)) {
            return false;
        }
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}