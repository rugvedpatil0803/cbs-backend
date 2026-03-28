package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.booking.CreateBookingRequest;
import com.project.cbsbackend.dto.booking.CreateBookingResponse;
import com.project.cbsbackend.dto.booking.UserBookingResponse;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin       = roles.contains("ADMIN");
        boolean isParticipant = roles.contains("PARTICIPANT");

        if (!isAdmin && !isParticipant) {
            throw new RuntimeException("You are not allowed to create a booking");
        }

        if (request.getSessionId() == null) {
            throw new RuntimeException("Session ID is required");
        }

        Long participantId;
        if (isAdmin && request.getParticipantId() != null) {
            participantId = request.getParticipantId();
        } else {
            participantId = requestingUserId;
        }

        SessionTemplate session = sessionTemplateRepository.findById(request.getSessionId())
                .filter(s -> !s.getIsDeleted() && s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        Availability availability = availabilityRepository
                .findBySessionIdAndIsDeletedFalse(session.getId())
                .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

        int remainingSeats = availability.getMaxSeat() - availability.getOccupiedSeats();

        if (remainingSeats <= 0) {
            throw new RuntimeException("No seats available for this session. All "
                    + availability.getMaxSeat() + " seats are occupied");
        }

        User participant = userRepository.findById(participantId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        List<String> participantRoles = userRoleLinkRepository.findByUserId(participantId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!participantRoles.contains("PARTICIPANT")) {
            throw new RuntimeException("Target user is not a participant");
        }

        Optional<Booking> existingBookingOpt =
                bookingRepository.findBySessionIdAndUserId(session.getId(), participantId);

        Booking booking;

        if (existingBookingOpt.isPresent()) {

            booking = existingBookingOpt.get();

            if (booking.getIsActive() && !booking.getIsDeleted()) {
                throw new RuntimeException("Participant has already booked this session");
            }

            booking.setIsActive(true);
            booking.setIsDeleted(false);
            booking.setBookingTime(LocalDateTime.now());

        } else {

            booking = Booking.builder()
                    .session(session)
                    .availability(availability)
                    .user(participant)
                    .coach(session.getCoach())
                    .isActive(true)
                    .isDeleted(false)
                    .build();
        }

        bookingRepository.save(booking);

        availability.setOccupiedSeats(availability.getOccupiedSeats() + 1);
        availabilityRepository.save(availability);

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin       = roles.contains("ADMIN");
        boolean isParticipant = roles.contains("PARTICIPANT");

        if (!isAdmin && !isParticipant) {
            throw new RuntimeException("You are not allowed to unenroll a booking");
        }

        Long participantId;
        if (isAdmin && targetParticipantId != null) {
            participantId = targetParticipantId;
        } else {
            participantId = requestingUserId;
        }

        Booking booking = bookingRepository
                .findBySessionIdAndUserIdAndIsDeletedFalse(sessionId, participantId)
                .orElseThrow(() -> new RuntimeException("Booking not found or already cancelled"));

        booking.setIsDeleted(true);
        booking.setIsActive(false);
        bookingRepository.save(booking);

        Availability availability = availabilityRepository
                .findBySessionIdAndIsDeletedFalse(sessionId)
                .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

        availability.setOccupiedSeats(Math.max(availability.getOccupiedSeats() - 1, 0));
        availabilityRepository.save(availability);
    }

    @Override
    @Transactional
    public List<UserBookingResponse> getUserBookings(Long requestingUserId, Long targetParticipantId) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin       = roles.contains("ADMIN");
        boolean isParticipant = roles.contains("PARTICIPANT");

        if (!isAdmin && !isParticipant) {
            throw new RuntimeException("You are not allowed to view bookings");
        }

        Long participantId;
        if (isAdmin && targetParticipantId != null) {
            participantId = targetParticipantId;
        } else {
            participantId = requestingUserId;
        }

        userRepository.findById(participantId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("Participant not found"));

        return bookingRepository.findAllActiveBookingsByUserId(participantId)
                .stream()
                .map(booking -> {
                    SessionTemplate session = booking.getSession();
                    User participant        = booking.getUser();
                    User coach              = session.getCoach();

                    return UserBookingResponse.builder()
                            .bookingId(booking.getId())
                            .bookingTime(booking.getBookingTime())
                            .sessionId(session.getId())
                            .sessionName(session.getName())
                            .sessionDescription(session.getDescription())
                            .startDay(session.getStartDay())
                            .endDay(session.getEndDay())
                            .startTime(session.getStartTime())
                            .endTime(session.getEndTime())
                            .metaData(session.getMetaData())
                            .coachId(coach.getId())
                            .coachName(coach.getFirstName() + " " + coach.getLastName())
                            .participantId(participant.getId())
                            .participantName(participant.getFirstName() + " " + participant.getLastName())
                            .build();
                })
                .toList();
    }

}