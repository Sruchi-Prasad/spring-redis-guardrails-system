package com.assignment.repository;

import com.assignment.entity.PostAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostAnalyticsRepository extends JpaRepository<PostAnalytics, Long> {
}
