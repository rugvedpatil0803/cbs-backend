package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Check duplicate booking
    boolean existsBySessionIdAndUserIdAndIsDeletedFalse(Long sessionId, Long userId);
    Optional<Booking> findBySessionIdAndUserIdAndIsDeletedFalse(Long sessionId, Long userId); // ← ADD
}