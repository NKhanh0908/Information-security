package com.infomationsecurity.mfa.redis;

public class RedisKeys {
    private RedisKeys() {}

    //{app}:{module}:{entity}:{identifier}:{attribute}

    public static final String REDIS_PREFIX = "hrm:";

    // Training Program Keys
    public static final String TRAINING_PROGRAM_PREFIX = REDIS_PREFIX + "training:program:";
    public static final String TRAINING_PROGRAMS_LIST = REDIS_PREFIX + "training:programs:list:";

    // Training Session Keys
    public static final String TRAINING_SESSIONS_LIST = REDIS_PREFIX + "training:sessions:list:";

    // Recruitment Keys
    public static final String RECRUITMENT_PREFIX = REDIS_PREFIX + "recruitment:";
    public static final String RECRUITMENT_LIST = REDIS_PREFIX + "recruitment:list:";

    // Employee Keys
    public static final String EMPLOYEE_PREFIX = REDIS_PREFIX + "employee:";
    public static final String EMPLOYEES_LIST = REDIS_PREFIX + "employees:list:";

    // OTP Keys
    public static final String OTP_PREFIX = REDIS_PREFIX + "auth:otp:";
    public static final String OTP_ATTEMPT_PREFIX = REDIS_PREFIX + "auth:otp-attempt:";

    public static String trainingProgramKey(Integer programId) {
        return TRAINING_PROGRAM_PREFIX + programId;
    }

    public static String recruitmentKey(Integer recruitmentId) {
        return RECRUITMENT_PREFIX + recruitmentId;
    }

    public static String employeeKey(Integer employeeId) {
        return EMPLOYEE_PREFIX + employeeId;
    }

    public static String otpKey(String email) {
        return OTP_PREFIX + email;
    }

    public static String otpAttemptKey(String email) {
        return OTP_ATTEMPT_PREFIX + email;
    }

}
