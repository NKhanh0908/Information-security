package com.infomationsecurity.mfa.config;

import com.infomationsecurity.mfa.util.LoginAttemptChecked;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.AccountLockedException;

@Component
public class BruteForceAuthenticationProvider implements AuthenticationProvider {
    private final AuthenticationProvider delegate;
    private final LoginAttemptChecked loginAttemptChecked;

    public BruteForceAuthenticationProvider(
            @Qualifier("daoAuthenticationProvider") AuthenticationProvider delegate,
            LoginAttemptChecked loginAttemptChecked) {
        this.delegate = delegate;
        this.loginAttemptChecked = loginAttemptChecked;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String username = authentication.getName();
        String clientIP = getClientIP();
        String key = username + ":" + clientIP;

        if (loginAttemptChecked.isBlocked(key)) {
            try {
                throw new AccountLockedException(
                        "Account temporarily locked due to too many failed attempts");
            } catch (AccountLockedException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            Authentication result = delegate.authenticate(authentication);
            loginAttemptChecked.loginSucceeded(key);
            return result;
        } catch (AuthenticationException e) {
            loginAttemptChecked.loginFailed(key);

            int remaining = loginAttemptChecked.getRemainingAttempts(key);
            if (remaining > 0) {
                throw new BadCredentialsException(
                        String.format("Invalid credentials. %d attempts remaining", remaining));
            } else {
                try {
                    throw new AccountLockedException(
                            "Account locked due to too many failed attempts");
                } catch (AccountLockedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    private String getClientIP() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
            return request.getRemoteAddr();
        }
        return "unknown";
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return delegate.supports(authentication);
    }
}
