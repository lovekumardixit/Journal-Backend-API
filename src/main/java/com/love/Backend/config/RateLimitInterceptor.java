package com.love.Backend.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    @Value("${app.rateLimiter.enabled:true}")
    private boolean rateLimiterEnabled;

    private static final int AUTH_MAX_REQUESTS_PER_MINUTE = 5;
    private static final int API_MAX_REQUESTS_PER_MINUTE = 100; 
    private static final long WINDOW_SECONDS = 60; 

    @Autowired 
    private RedisTemplate<String, Object> redisTemplate; 

    @Override 
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception { 
        // If rate limiter disabled via configuration (dev/tests), allow all requests
        if (!rateLimiterEnabled) {
            return true;
        }

        String clientKey = buildClientKey(request);
        int maxRequests = isAuthRequest(request) ? AUTH_MAX_REQUESTS_PER_MINUTE : API_MAX_REQUESTS_PER_MINUTE; 
        Long currentCount; 
        try { 
            currentCount = redisTemplate.opsForValue().increment(clientKey); 
        } catch (Exception ex) { 
            return true; 
        }
        if (currentCount != null && currentCount == 1L) { 
            redisTemplate.expire(clientKey, WINDOW_SECONDS, TimeUnit.SECONDS); 
        }
        if (currentCount != null && currentCount > maxRequests) { 
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value()); 
            response.setContentType("application/json"); 
            response.getWriter().write("{\"message\":\"Rate limit exceeded. Try again later.\"}"); 
            return false; 
        }
        return true; 
    }

    private String buildClientKey(HttpServletRequest request) { 
        String bucket = isAuthRequest(request) ? "auth" : "api"; 
        return "rate-limit:" + bucket + ":" + getClientIP(request); 
    }

    private boolean isAuthRequest(HttpServletRequest request) { 
        String path = request.getRequestURI(); 
        return path.contains("/auth/") || path.contains("/public/create-user"); 
    }

    private String getClientIP(HttpServletRequest request) { 
        String xForwardedFor = request.getHeader("X-Forwarded-For"); 
        if (xForwardedFor != null && !xForwardedFor.isBlank()) { 
            return xForwardedFor.split(",")[0].trim(); 
        }
        return request.getRemoteAddr(); 
    }
}
