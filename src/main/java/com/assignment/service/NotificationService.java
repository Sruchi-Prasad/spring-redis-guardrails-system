package com.assignment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final StringRedisTemplate redisTemplate;
    private final RedisGuardService redisGuard;

    public void handleBotNotification(Long userId, String message) {
        String cooldownKey = "user:" + userId + ":notif_cooldown";

        if (!redisGuard.exists(cooldownKey)) {
            log.info("Push Notification Sent to user {}: {}", userId, message);
            redisGuard.setWithTTL(cooldownKey, "1", 15);
        } else {
            log.info("Notification batched for user {}: {}", userId, message);
            redisTemplate.opsForList().rightPush("user:" + userId + ":pending_notifs", message);
        }
    }
}
