package com.infomationsecurity.mfa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Component
@EnableAsync
public class AsyncConfig {
    @Bean(name = "mfaTaskExecutor")
    public TaskExecutor mfaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mfa-async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "loginAttemptSuccessTaskExecutor")
    public TaskExecutor loginAttemptSuccessTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("login-attempt-success-async-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "loginAttemptFailedTaskExecutor")
    public TaskExecutor loginAttemptFailedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("login-attempt-failed-async-");
        executor.initialize();
        return executor;
    }
}
