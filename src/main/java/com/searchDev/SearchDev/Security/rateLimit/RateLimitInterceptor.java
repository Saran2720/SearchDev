package com.searchDev.SearchDev.Security.rateLimit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.searchDev.SearchDev.Security.RateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Autowired
    RateLimitInterceptor(RateLimitService rateLimitService){
        this.rateLimitService = rateLimitService;
    }

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler
    ){
        if(!(handler instanceof HandlerMethod method)){
            return true;
        }

        RateLimiter rateLimiter = method.getMethodAnnotation(RateLimiter.class);
        if(rateLimiter==null) return true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();

        boolean allowed = rateLimitService.isAllowed(userId, request.getRequestURI(), rateLimiter.request(), rateLimiter.durationSeconds());

        if(!allowed){
            response.setStatus(429);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Too many request");
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        }
        return true;

    }


}
