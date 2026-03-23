package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.CreateBookingRequest;
import com.project.cbsbackend.dto.CreateBookingResponse;
import com.project.cbsbackend.entity.Availability;
import com.project.cbsbackend.entity.Booking;
import com.project.cbsbackend.entity.SessionTemplate;
import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.repository.AvailabilityRepository;
import com.project.cbsbackend.repository.BookingRepository;
import com.project.cbsbackend.repository.SessionTemplateRepository;
import com.project.cbsbackend.repository.UserRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;
import com.project.cbsbackend.service.BookingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SessionTemplateRepository sessionTemplateRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;

    @Override
    @Transactional
    public CreateBookingResponse createBooking(Long requestingUserId, CreateBookingRequest request) {

        // ── 1. Get roles of requesting user ───────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin       = roles.contains("ADMIN");
        boolean isParticipant = roles.contains("PARTICIPANT");

        // ── 2. Only PARTICIPANT or ADMIN allowed ──────────────────────
        if (!isAdmin && !isParticipant) {
            throw new RuntimeException("You are not allowed to create a booking");
        }

        // ── 3. Validate required fields ───────────────────────────────
        if (request.getSessionId() == null) {
            throw new RuntimeException("Session ID is required");
        }

        // ── 4. Determine participantId ────────────────────────────────
        Long participantId;
        if (isAdmin && request.getParticipantId() != null) {
            participantId = request.getParticipantId();
        } else {
            participantId = requestingUserId;
        }

        // ── 5. Fetch session ──────────────────────────────────────────
        SessionTemplate session = sessionTemplateRepository.findById(request.getSessionId())
                .filter(s -> !s.getIsDeleted() && s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        // ── 6. Fetch availability from tbl_availability ───────────────
        Availability availability = availabilityRepository
                .findBySessionIdAndIsDeletedFalse(session.getId())
                .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

        // ── 7. Check seats available ──────────────────────────────────
        int remainingSeats = availability.getMaxSeat() - availability.getOccupiedSeats();

        if (remainingSeats <= 0) {
            throw new RuntimeException("No seats available for this session. All "
                    + availability.getMaxSeat() + " seats are occupied");
        }

        // ── 8. Fetch participant ──────────────────────────────────────
        User participant = userRepository.findById(participantId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        // ── 9. Verify target user is actually a PARTICIPANT ───────────
        List<String> participantRoles = userRoleLinkRepository.findByUserId(participantId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!participantRoles.contains("PARTICIPANT")) {
            throw new RuntimeException("Target user is not a participant");
        }

        // ── 10. Check duplicate booking ───────────────────────────────
        boolean alreadyBooked = bookingRepository
                .existsBySessionIdAndUserIdAndIsDeletedFalse(session.getId(), participantId);

        if (alreadyBooked) {
            throw new RuntimeException("Participant has already booked this session");
        }

        // ── 11. Create booking ────────────────────────────────────────
        Booking booking = Booking.builder()
                .session(session)
                .availability(availability)
                .user(participant)
                .coach(session.getCoach())
                .isActive(true)
                .isDeleted(false)
                .build();

        bookingRepository.save(booking);

        // ── 12. Increment occupied seats in tbl_availability ──────────
        availability.setOccupiedSeats(availability.getOccupiedSeats() + 1);
        availabilityRepository.save(availability);

        // ── 13. Return response with updated remaining seats ──────────
        int updatedRemainingSeats = availability.getMaxSeat() - availability.getOccupiedSeats();

        return CreateBookingResponse.builder()
                .bookingId(booking.getId())
                .sessionId(session.getId())
                .sessionName(session.getName())
                .participantId(participant.getId())
                .participantName(participant.getFirstName() + " " + participant.getLastName())
                .coachId(session.getCoach().getId())
                .coachName(session.getCoach().getFirstName() + " " + session.getCoach().getLastName())
                .availabilityId(availability.getId())
                .remainingSeats(updatedRemainingSeats)
                .bookingTime(booking.getBookingTime())
                .build();
    }

    @Override
    @Transactional
    public void unenrollBooking(Long requestingUserId, Long sessionId, Long targetParticipantId) {

        // ── 1. Get roles of requesting user ───────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin       = roles.contains("ADMIN");
        boolean isParticipant = roles.contains("PARTICIPANT");

        // ── 2. Only PARTICIPANT or ADMIN allowed ──────────────────────
        if (!isAdmin && !isParticipant) {
            throw new RuntimeException("You are not allowed to unenroll a booking");
        }

        // ── 3. Determine whose booking to cancel ─────────────────────
        Long participantId;
        if (isAdmin && targetParticipantId != null) {
            // Admin cancelling a specific participant's booking
            participantId = targetParticipantId;
        } else {
            // Participant cancelling their own booking
            participantId = requestingUserId;
        }

        // ── 4. Fetch booking by sessionId + participantId ─────────────
        Booking booking = bookingRepository
                .findBySessionIdAndUserIdAndIsDeletedFalse(sessionId, participantId)
                .orElseThrow(() -> new RuntimeException("Booking not found or already cancelled"));

        // ── 5. Soft delete booking ────────────────────────────────────
        booking.setIsDeleted(true);
        booking.setIsActive(false);
        bookingRepository.save(booking);

        // ── 6. Decrement occupied seats in tbl_availability ───────────
        Availability availability = availabilityRepository
                .findBySessionIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

        availability.setOccupiedSeats(Math.max(availability.getOccupiedSeats() - 1, 0));
        availabilityRepository.save(availability);
    }

}