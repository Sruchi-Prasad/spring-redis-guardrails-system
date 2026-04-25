package com.assignment.scheduler;

import com.assignment.entity.PostAnalytics;
import com.assignment.repository.PostAnalyticsRepository;
import com.assignment.service.RedisGuardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSweeper {

    private final StringRedisTemplate redisTemplate;
    private final PostAnalyticsRepository analyticsRepo;
    private final RedisGuardService redisGuard;

    @Async
    @Scheduled(cron = "0 */5 * * * *")
    public void sweepAndAggregate() {
        log.info("Starting scheduled analytics aggregation sweep...");

        // 1. Process pending notifications
        Set<String> notifKeys = redisTemplate.keys("user:*:pending_notifs");
        if (notifKeys != null) {
            for (String key : notifKeys) {
                List<String> msgs = redisTemplate.opsForList().range(key, 0, -1);
                if (msgs != null && !msgs.isEmpty()) {
                    Long userId = Long.parseLong(key.split(":")[2]); // user:userId:pending_notifs -> index 1 or 2
                                                                     // depending on colon placement
                    log.info("Summarized Push for user {}: {} and {} other interactions.",
                            userId, msgs.get(0), msgs.size() - 1);
                    redisGuard.delete(key);
                }
            }
        }

        // 2. Aggregate Virality and Bot Metrics
        Set<String> postKeys = redisTemplate.keys("post:*:virality_score");
        if (postKeys != null) {
            for (String key : postKeys) {
                Long postId = Long.parseLong(key.split(":")[1]);
                Long score = redisGuard.getValue(key);
                Long botCount = redisGuard.getValue("post:" + postId + ":bot_count");

                PostAnalytics analytics = new PostAnalytics();
                analytics.setPostId(postId);
                analytics.setViralityScore(score);
                analytics.setBotInteractions(botCount);
                analytics.setSnapshotTime(LocalDateTime.now());

                analyticsRepo.save(analytics);
                log.info("Aggregated analytics for post {}: ViralityScore={}, BotCount={}", postId, score, botCount);
            }
        }
    }
}
