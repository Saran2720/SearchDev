package com.searchDev.SearchDev.Security;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    int request();
    int durationSeconds();
}
