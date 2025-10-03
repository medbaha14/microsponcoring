package com.example.microsponsoringbackend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.microsponsoringbackend.interceptor.PerformanceMonitoringInterceptor;

import java.util.Arrays;
import java.util.concurrent.Executor;

@Configuration
@EnableCaching
@EnableAsync
public class ApplicationConfig implements WebMvcConfigurer {
    
    @Autowired
    private PerformanceMonitoringInterceptor performanceMonitoringInterceptor;
    
    // Cache manager is now defined in CacheConfig.java to avoid bean conflicts
    
    /**
     * Configure async executor for performance monitoring
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Performance-");
        executor.initialize();
        return executor;
    }
    
    /**
     * Register the performance monitoring interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(performanceMonitoringInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/favicon.ico");
    }
}
