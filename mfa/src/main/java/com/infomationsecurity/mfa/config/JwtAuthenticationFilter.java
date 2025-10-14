package com.infomationsecurity.mfa.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infomationsecurity.mfa.dto.response.APIResponse;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.service.impl.OurUserDetailsService;
import com.infomationsecurity.mfa.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final OurUserDetailsService ourUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthenticationFilter(OurUserDetailsService ourUserDetailsService, JwtTokenUtil jwtTokenUtil) {
        this.ourUserDetailsService = ourUserDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwtToken;
        final String userEmail;

        try {
            if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            jwtToken = authHeader.substring(7); // "Bearer " -> Get token
            userEmail = jwtTokenUtil.extractTokenGetUsername(jwtToken);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = ourUserDetailsService.loadUserByUsername(userEmail);

                if (!jwtTokenUtil.isTokenValid(jwtToken, userDetails)) {
                    sendErrorResponse(response, Error.JWT_EXPIRED);
                    return;
                }

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            logger.info("Token has expired");
            sendErrorResponse(response, Error.JWT_EXPIRED);
        } catch (MalformedJwtException | IllegalArgumentException e) {
            logger.warn("Malformed or invalid JWT token", e);
            sendErrorResponse(response, Error.JWT_MALFORMED);
        }
    }

    // Phương thức helper để tạo response lỗi
    private void sendErrorResponse(HttpServletResponse response, Error error) throws IOException {
        response.setStatus(error.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Tạo một object response tương tự như trong CustomizedResponseEntityExceptionHandler
        APIResponse<Object> apiResponse = new APIResponse<>(
                false,
                error.getMessage(),
                null,
                Collections.singletonList(error.getMessage()),
                null // request.getDescription(false) không có sẵn ở đây
        );

        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }

}