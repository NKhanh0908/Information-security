package com.infomationsecurity.mfa.redis;

public class RedisKeys {
    private RedisKeys() {}

    //{app}:{module}:{entity}:{identifier}:{attribute}

    public static final String REDIS_PREFIX = "mfa-app:";

    // OTP Keys
    public static final String OTP_PREFIX = REDIS_PREFIX + "auth:otp:";
    public static final String OTP_ATTEMPT_PREFIX = REDIS_PREFIX + "auth:otp-attempt:";

    public static String otpKey(String email) {
        return OTP_PREFIX + email;
    }

    public static String otpAttemptKey(String email) {
        return OTP_ATTEMPT_PREFIX + email;
    }

}
