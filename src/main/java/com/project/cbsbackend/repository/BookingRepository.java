package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);                            // ← ADD
    boolean existsBySessionIdAndUserIdAndIsDeletedFalse(Long sessionId, Long userId);

    Optional<Booking> findBySessionIdAndUserId(Long sessionId, Long userId);
    Optional<Booking> findBySessionIdAndUserIdAndIsDeletedFalse(Long sessionId, Long userId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.session s
        JOIN FETCH s.coach c
        JOIN FETCH b.user u
        WHERE b.user.id = :participantId
        AND b.isDeleted = false
        ORDER BY b.bookingTime DESC
    """)
    List<Booking> findAllActiveBookingsByUserId(@Param("participantId") Long participantId);
}