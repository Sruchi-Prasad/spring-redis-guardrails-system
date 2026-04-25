package com.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisGuardService {

    private final StringRedisTemplate redisTemplate;

    public void increment(String key, long value) {
        try {
            redisTemplate.opsForValue().increment(key, value);
        } catch (Exception e) {
            log.error("Redis increment failed for key: {}. Error: {}", key, e.getMessage());
            throw e;
        }
    }

    public Long getValue(String key) {
        try {
            String val = redisTemplate.opsForValue().get(key);
            return val == null ? 0L : Long.parseLong(val);
        } catch (Exception e) {
            log.error("Redis get failed for key: {}. Error: {}", key, e.getMessage());
            return 0L;
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis exists check failed for key: {}. Error: {}", key, e.getMessage());
            return false;
        }
    }

    public void setWithTTL(String key, String value, long minutes) {
        try {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(minutes));
        } catch (Exception e) {
            log.error("Redis setWithTTL failed for key: {}. Error: {}", key, e.getMessage());
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Redis delete failed for key: {}. Error: {}", key, e.getMessage());
        }
    }

    /**
     * Checks if an action is idempotent within a 15-minute window.
     */
    public boolean isIdempotent(Long userId, Long postId, String actionType) {
        String key = String.format("user:action:%d:%d:%s", userId, postId, actionType);

        if (exists(key)) {
            log.warn("ACTION_BLOCKED: Duplicate {} action detected for user {} on post {}", actionType, userId, postId);
            return false;
        }

        setWithTTL(key, "1", 15);
        return true;
    }
}
