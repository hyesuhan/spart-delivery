package com.sparta.spartadelivery.global.infrastructure.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig {

    @Bean(name = "eventTaskExecutor")
    public Executor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // default thread size
        executor.setMaxPoolSize(20); // max thread size
        executor.setQueueCapacity(50); // waiting queue size
        executor.setThreadNamePrefix("EventExecutor-");
        return executor;
    }
}
