package com.assignment.service;

import com.assignment.entity.Comment;
import com.assignment.exception.RateLimitExceededException;
import com.assignment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepo;
    private final RedisGuardService redisGuard;
    private final PostService postService;
    private final NotificationService notificationService;

    /**
     * Fully guardrailed comment submission:
     * - Idempotency Lock
     * - Depth Level Safety (Max 20)
     * - Bot Cooldown Enforcement
     */
    public Comment addComment(Long postId, Comment comment, Long userId, boolean isBot) {
        // 1. Idempotency Check
        if (!redisGuard.isIdempotent(userId, postId, "COMMENT")) {
            throw new IllegalStateException("Duplicate comment detected");
        }

        // 2. Vertical Guardrail (Max Depth) - Prevents recursive comment chains
        if (comment.getDepthLevel() != null && comment.getDepthLevel() > 20) {
            log.warn("ACTION_BLOCKED: Max depth exceeded for post {}", postId);
            throw new IllegalStateException("Comment depth limit exceeded");
        }

        // 3. Horizontal Guardrail (Bot Cooldown/Limit)
        if (isBot) {
            String botCooldownKey = "user:bot_cooldown:" + userId;
            if (redisGuard.exists(botCooldownKey)) {
                log.warn("ACTION_BLOCKED: Bot {} on cooldown", userId);
                throw new RateLimitExceededException("Bot interaction restricted due to cooldown");
            }
            // Set 10 min cooldown for this bot
            redisGuard.setWithTTL(botCooldownKey, "1", 10);

            // Trigger notification with 15-min batching logic
            notificationService.handleBotNotification(userId, "New bot interaction on post " + postId);
        }

        // 4. Persistence
        comment.setPostId(postId);
        comment.setAuthorId(userId);
        comment.setCreatedAt(LocalDateTime.now());
        Comment saved = commentRepo.save(comment);

        // 5. Update Virality Engine
        postService.updateVirality(postId, "COMMENT", isBot);

        return saved;
    }

    public List<Comment> getCommentsForPost(Long postId) {
        return commentRepo.findByPostId(postId);
    }
}
