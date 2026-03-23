package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
    Optional<Availability> findBySessionId(Long sessionId);
    Optional<Availability> findBySessionIdAndIsDeletedFalse(Long sessionId);  // ← ADD this
}