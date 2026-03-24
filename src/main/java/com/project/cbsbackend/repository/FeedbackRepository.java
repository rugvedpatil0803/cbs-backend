package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Check duplicate
    boolean existsByUserIdAndSessionIdAndIsDeletedFalse(Long userId, Long sessionId);

    // Get all feedback for a particular session
    @Query("""
        SELECT f FROM Feedback f
        JOIN FETCH f.user u
        JOIN FETCH f.session s
        WHERE f.session.id = :sessionId
        AND f.isDeleted = false
        ORDER BY f.createdAt DESC
    """)
    List<Feedback> findAllBySessionId(@Param("sessionId") Long sessionId);

    // Get all feedback by a particular user
    @Query("""
        SELECT f FROM Feedback f
        JOIN FETCH f.user u
        JOIN FETCH f.session s
        WHERE f.user.id = :userId
        AND f.isDeleted = false
        ORDER BY f.createdAt DESC
    """)
    List<Feedback> findAllByUserId(@Param("userId") Long userId);
}