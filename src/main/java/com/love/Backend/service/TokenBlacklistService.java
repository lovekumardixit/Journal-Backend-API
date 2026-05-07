package com.love.Backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void blacklistToken(String token, long expiryMillis){
        long ttl = expiryMillis - System.currentTimeMillis();

        if(ttl > 0){
            redisTemplate.opsForValue().set(token, "blacklisted", ttl, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token){
        return redisTemplate.hasKey(token);
    }
}
