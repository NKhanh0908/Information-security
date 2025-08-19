package com.infomationsecurity.mfa.config;

import com.infomationsecurity.mfa.service.impl.OurUserDetailsService;
import com.infomationsecurity.mfa.util.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

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
                    throw new RuntimeException("Token is INVALID");
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
            throw new RuntimeException("Token is EXPIRED");
        } catch (MalformedJwtException | IllegalArgumentException e) {
            throw new RuntimeException("Token is MALFORMED");
        }
    }

}