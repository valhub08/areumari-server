package com.example.areumariserver.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;

    public void save(String username, String refreshToken) {
        redisTemplate.opsForValue().set(username, refreshToken, 7, TimeUnit.DAYS);
    }

    public String findByToken(String refreshToken) {
        return redisTemplate.keys("*").stream()
                .filter(key -> refreshToken.equals(redisTemplate.opsForValue().get(key)))
                .findFirst()
                .orElse(null);
    }

    public void delete(String username) {
        redisTemplate.delete(username);
    }
}