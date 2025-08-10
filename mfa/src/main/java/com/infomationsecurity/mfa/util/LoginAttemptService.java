package com.infomationsecurity.mfa.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LoginAttemptService {

    private final int MAX_ATTEMPTS = 5;
    private final int LOCKOUT_DURATION_MINUTES = 10;

    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        this.attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(LOCKOUT_DURATION_MINUTES, TimeUnit.MINUTES)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void loginSucceeded(String key) {
        attemptsCache.invalidate(key);
    }

    public void loginFailed(String key) {
        int attempts = attemptsCache.getUnchecked(key);
        attempts++;
        if (attempts >= MAX_ATTEMPTS) {
            log.warn("Too many login attempts for key: {}", key);
        }
        attemptsCache.put(key, attempts);
    }

    public boolean isBlocked(String key) {
        try {
            int attempts = attemptsCache.getUnchecked(key);
            return attempts >= MAX_ATTEMPTS;
        } catch (Exception e) {
            log.error("Error checking login attempts for key: {}", key, e);
            return false;
        }
    }

    public int getRemainingAttempts(String key) {
        try {
            int attempts = attemptsCache.getUnchecked(key);
            return MAX_ATTEMPTS - attempts;
        } catch (Exception e) {
            log.error("Error getting remaining attempts for key: {}", key, e);
            return MAX_ATTEMPTS; // If there's an error, assume no attempts have been made
        }
    }
}
