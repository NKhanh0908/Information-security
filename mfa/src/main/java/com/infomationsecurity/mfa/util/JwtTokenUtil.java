package com.infomationsecurity.mfa.util;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
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

    private final SecretKey secretKeyForAccessToken ;
    private final SecretKey secretKeyForRefreshToken = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    private static final long EXPIRATION_TIME_FOR_TOKEN = 2_592_000_000L; // 1 Month (30 Days)
    private static final long EXPIRATION_TIME_FOR_REFRESH_TOKEN = 2_592_000_000L; // 1 Month (30 Days)

    public JwtTokenUtil(){
        String secreteString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";
        byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));
        this.secretKeyForAccessToken = new SecretKeySpec(keyBytes, "HmacSHA256");
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
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_FOR_TOKEN))
                .signWith(secretKeyForAccessToken)
                .compact();
    }

    public String generateRefreshToken( UserDetails userDetails){
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claims(extractRole(userDetails))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_FOR_REFRESH_TOKEN))
                .signWith(secretKeyForRefreshToken)
                .compact();
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(
                Jwts.parser().verifyWith(secretKeyForAccessToken).build().parseSignedClaims(token).getPayload()
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
            throw new UsernameNotFoundException("null");
        }
        if (isTokenExpired(token)) {
            throw new UsernameNotFoundException("null");
        }
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

}