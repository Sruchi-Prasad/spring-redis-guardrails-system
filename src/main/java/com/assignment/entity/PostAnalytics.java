package com.assignment.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class PostAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private long totalLikes;
    private long totalComments;
    private long botInteractions;
    private long humanInteractions;
    private long viralityScore;
    private LocalDateTime snapshotTime;
}
