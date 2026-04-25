package com.assignment.service;

import com.assignment.entity.Post;
import com.assignment.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepo;
    private final RedisGuardService redisGuard;

    public Post createPost(Post post) {
        post.setCreatedAt(LocalDateTime.now());
        return postRepo.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepo.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepo.findById(id);
    }

    /**
     * Updates post virality based on interaction type and actor status.
     * Likes increase score by 20, Comments by 50.
     * Bot interactions are capped at 100 per post to prevent artificial
     * manipulation.
     */
    public void updateVirality(Long postId, String type, boolean isBot) {
        String scoreKey = "post:" + postId + ":virality_score";
        String botCountKey = "post:" + postId + ":bot_count";

        try {
            if (isBot) {
                redisGuard.increment(botCountKey, 1);
                Long currentBotCount = redisGuard.getValue(botCountKey);
                if (currentBotCount <= 100) {
                    redisGuard.increment(scoreKey, 1);
                } else {
                    log.info("Bot virality cap reached for post {}.", postId);
                }
            } else {
                long increment = type.equals("LIKE") ? 20 : 50;
                redisGuard.increment(scoreKey, increment);
            }
        } catch (Exception e) {
            log.error("DEGRADED_MODE_ACTIVATED: Redis failure for post: {}", postId);
        }
    }

    public void likePost(Long postId, Long userId) {
        if (!redisGuard.isIdempotent(userId, postId, "LIKE")) {
            return;
        }
        updateVirality(postId, "LIKE", false);
    }

    public Long getViralityScore(Long postId) {
        return redisGuard.getValue("post:" + postId + ":virality_score");
    }
}
