package com.searchDev.SearchDev.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.searchDev.SearchDev.Security.rateLimit.RateLimitInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer{
    private final RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    WebConfig(RateLimitInterceptor rateLimitInterceptor){
        this.rateLimitInterceptor= rateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor);
    }

}
