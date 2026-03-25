package com.project.cbsbackend.repository;

import com.project.cbsbackend.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);
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

    // ── NEW: for session details ──────────────────────────────────────────────

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.user u
        LEFT JOIN FETCH u.userInfo
        WHERE b.session.id = :sessionId
        AND b.isActive = true AND b.isDeleted = false
    """)
    List<Booking> findActiveBookingsBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.user u
        LEFT JOIN FETCH u.userInfo
        WHERE b.session.id = :sessionId
    """)
    List<Booking> findAllBookingsBySessionId(@Param("sessionId") Long sessionId);

    @Query("""
        SELECT b FROM Booking b
        JOIN FETCH b.session s
        JOIN FETCH s.coach c
        WHERE b.user.id = :userId
        ORDER BY b.bookingTime DESC
    """)
    List<Booking> findAllBookingsByUserId(@Param("userId") Long userId);

    long countBySessionId(Long sessionId);

    long countBySessionIdAndIsActiveTrueAndIsDeletedFalse(Long sessionId);

    long countBySessionIdAndIsActiveFalseAndIsDeletedFalse(Long sessionId);

    long countBySessionIdAndIsDeletedTrue(Long sessionId);

    List<Booking> findBySessionIdAndIsDeletedFalseOrderByBookingTimeDesc(Long sessionId);

    @Query("""
    SELECT b FROM Booking b
    JOIN FETCH b.user u
    LEFT JOIN FETCH u.userInfo
    WHERE b.session.id = :sessionId
    AND b.isDeleted = false
    ORDER BY b.bookingTime DESC
""")
    List<Booking> findAllValidBookingsBySessionId(@Param("sessionId") Long sessionId);

    long countBySessionIdAndIsDeletedFalse(Long sessionId);

}