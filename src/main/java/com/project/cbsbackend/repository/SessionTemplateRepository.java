package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.SessionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SessionTemplateRepository extends JpaRepository<SessionTemplate, Long> {

    // Upcoming: startDay is strictly in the future
    @Query("SELECT s FROM SessionTemplate s JOIN FETCH s.coach WHERE s.isDeleted = false AND s.isActive = true AND s.startDay > :today")
    List<SessionTemplate> findUpcomingSessions(@Param("today") LocalDate today);

    @Query("SELECT s FROM SessionTemplate s JOIN FETCH s.coach WHERE s.isDeleted = false AND s.isActive = true AND s.startDay <= :today AND s.endDay >= :today")
    List<SessionTemplate> findOngoingSessions(@Param("today") LocalDate today);

    @Query("SELECT s FROM SessionTemplate s JOIN FETCH s.coach WHERE s.isDeleted = false AND s.isActive = true AND s.endDay < :today")
    List<SessionTemplate> findCompletedSessions(@Param("today") LocalDate today);
}