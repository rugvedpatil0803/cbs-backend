package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.*;
import com.project.cbsbackend.entity.*;
import com.project.cbsbackend.repository.*;
import com.project.cbsbackend.service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionTemplateRepository sessionTemplateRepository;
    private final UserRepository userRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final AvailabilityRepository availabilityRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public CreateSessionResponse createSession(Long requestingUserId, CreateSessionRequest request) {

        // ── 1. Get roles of requesting user ───────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        // ── 2. Only COACH or ADMIN allowed ────────────────────────────
        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to create a session");
        }

        // ── 3. Determine coachId ──────────────────────────────────────
        Long coachId;
        if (isAdmin && request.getCoachId() != null) {
            coachId = request.getCoachId();
        } else {
            coachId = requestingUserId;
        }

        // ── 4. Validate basic fields ──────────────────────────────────
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Session name is required");
        }
        if (request.getStartDay() == null || request.getEndDay() == null) {
            throw new RuntimeException("Start day and end day are required");
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (request.getEndDay().isBefore(request.getStartDay())) {
            throw new RuntimeException("End day cannot be before start day");
        }
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time cannot be before start time");
        }
        if (request.getNoOfSeats() == null || request.getNoOfSeats() <= 0) {
            throw new RuntimeException("Number of seats must be greater than 0");
        }

        // ── 5. Fetch coach user ───────────────────────────────────────
        User coach = userRepository.findById(coachId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        // ── 6. Verify the target user is actually a COACH ─────────────
        List<String> coachRoles = userRoleLinkRepository.findByUserId(coachId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!coachRoles.contains("COACH")) {
            throw new RuntimeException("Target user is not a coach");
        }

        // ── 7. Create and save session ────────────────────────────────
        SessionTemplate session = SessionTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .coach(coach)
                .startDay(request.getStartDay())
                .endDay(request.getEndDay())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .noOfSeats(request.getNoOfSeats())
                .metaData(request.getMetaData())
                .isActive(true)
                .isDeleted(false)
                .build();

        sessionTemplateRepository.save(session);

        // ── 8. Auto create availability entry ─────────────────────────
        Availability availability = Availability.builder()
                .session(session)
                .maxSeat(request.getNoOfSeats())
                .occupiedSeats(0)
                .isActive(true)
                .isDeleted(false)
                .build();

        availabilityRepository.save(availability);

        // ── 9. Return response ────────────────────────────────────────
        return CreateSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .coachId(coach.getId())
                .coachName(coach.getFirstName() + " " + coach.getLastName())
                .startDay(session.getStartDay())
                .endDay(session.getEndDay())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .noOfSeats(session.getNoOfSeats())
                .metaData(session.getMetaData())
                .build();
    }

    @Override
    @Transactional
    public CreateSessionResponse updateSession(Long requestingUserId, Long sessionId, UpdateSessionRequest request) {

        // ── 1. Get roles of requesting user ───────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        // ── 2. Only COACH or ADMIN allowed ────────────────────────────
        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to update a session");
        }

        // ── 3. Fetch session ──────────────────────────────────────────
        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ── 4. Coach can only update their own session ────────────────
        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to update another coach's session");
        }

        // ── 5. Validate date/time if provided ─────────────────────────
        LocalDate startDay  = request.getStartDay()  != null ? request.getStartDay()  : session.getStartDay();
        LocalDate endDay    = request.getEndDay()     != null ? request.getEndDay()    : session.getEndDay();
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : session.getStartTime();
        LocalTime endTime   = request.getEndTime()   != null ? request.getEndTime()   : session.getEndTime();

        if (endDay.isBefore(startDay)) {
            throw new RuntimeException("End day cannot be before start day");
        }
        if (endTime.isBefore(startTime)) {
            throw new RuntimeException("End time cannot be before start time");
        }
        if (request.getNoOfSeats() != null && request.getNoOfSeats() <= 0) {
            throw new RuntimeException("Number of seats must be greater than 0");
        }

        // ── 6. Partial update session fields ──────────────────────────
        if (request.getName()        != null) session.setName(request.getName());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getStartDay()    != null) session.setStartDay(request.getStartDay());
        if (request.getEndDay()      != null) session.setEndDay(request.getEndDay());
        if (request.getStartTime()   != null) session.setStartTime(request.getStartTime());
        if (request.getEndTime()     != null) session.setEndTime(request.getEndTime());
        if (request.getNoOfSeats()   != null) session.setNoOfSeats(request.getNoOfSeats());
        if (request.getMetaData()    != null) session.setMetaData(request.getMetaData());

        sessionTemplateRepository.save(session);

        // ── 7. Update availability maxSeat if noOfSeats changed ───────
        if (request.getNoOfSeats() != null) {
            Availability availability = availabilityRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

            // Make sure new maxSeat is not less than already occupied seats
            if (request.getNoOfSeats() < availability.getOccupiedSeats()) {
                throw new RuntimeException("New seat count cannot be less than already occupied seats ("
                        + availability.getOccupiedSeats() + ")");
            }

            availability.setMaxSeat(request.getNoOfSeats());
            availabilityRepository.save(availability);
        }

        // ── 8. Return response ────────────────────────────────────────
        return CreateSessionResponse.builder()
                .id(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .coachId(session.getCoach().getId())
                .coachName(session.getCoach().getFirstName() + " " + session.getCoach().getLastName())
                .startDay(session.getStartDay())
                .endDay(session.getEndDay())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .noOfSeats(session.getNoOfSeats())
                .metaData(session.getMetaData())
                .build();
    }

    // ── Private helper: map session + availability → response ──────────────────
    private SessionWithAvailabilityResponse mapToSessionWithAvailability(SessionTemplate session) {
        Availability availability = availabilityRepository.findBySessionId(session.getId())
                .orElse(null);

        int maxSeat      = availability != null ? availability.getMaxSeat()      : 0;
        int occupiedSeats = availability != null ? availability.getOccupiedSeats() : 0;

        return SessionWithAvailabilityResponse.builder()
                .sessionId(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .coachId(session.getCoach().getId())
                .coachName(session.getCoach().getFirstName() + " " + session.getCoach().getLastName())
                .startDay(session.getStartDay())
                .endDay(session.getEndDay())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .noOfSeats(session.getNoOfSeats())
                .metaData(session.getMetaData())
                .maxSeat(maxSeat)
                .occupiedSeats(occupiedSeats)
                .availableSeats(maxSeat - occupiedSeats)
                .build();
    }

    @Override
    @Transactional  // ← ADD THIS
    public List<SessionWithAvailabilityResponse> getUpcomingSessions() {
        return sessionTemplateRepository.findUpcomingSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional  // ← ADD THIS
    public List<SessionWithAvailabilityResponse> getOngoingSessions() {
        return sessionTemplateRepository.findOngoingSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional  // ← ADD THIS
    public List<SessionWithAvailabilityResponse> getCompletedSessions() {
        return sessionTemplateRepository.findCompletedSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional
    public MySessionsResponse getMySessions(Long requestingUserId) {

        // Verify the user is a COACH
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!roles.contains("COACH") && !roles.contains("ADMIN")) {
            throw new RuntimeException("You are not a coach");
        }

        LocalDate today = LocalDate.now();

        List<SessionWithAvailabilityResponse> upcoming = sessionTemplateRepository
                .findUpcomingSessionsByCoach(today, requestingUserId)
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();

        List<SessionWithAvailabilityResponse> ongoing = sessionTemplateRepository
                .findOngoingSessionsByCoach(today, requestingUserId)
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();

        List<SessionWithAvailabilityResponse> completed = sessionTemplateRepository
                .findCompletedSessionsByCoach(today, requestingUserId)
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();

        return MySessionsResponse.builder()
                .upcoming(upcoming)
                .ongoing(ongoing)
                .completed(completed)
                .build();
    }

    @Override
    @Transactional
    public SessionDetailResponse getSessionDetails(Long requestingUserId, Long sessionId, boolean isAdmin) {

        // ── 1. Fetch session ──────────────────────────────────────
        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ── 2. Auth check ─────────────────────────────────────────
        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to view this session");
        }

        // ── 3. Fetch availability ─────────────────────────────────
        Availability availability = availabilityRepository.findBySessionId(sessionId)
                .orElse(null);

        int maxSeat       = availability != null ? availability.getMaxSeat()       : 0;
        int occupiedSeats = availability != null ? availability.getOccupiedSeats() : 0;

        // ── 4. Fetch bookings (admin sees all, coach sees active only) ─
        List<Booking> bookings = isAdmin
                ? bookingRepository.findAllBookingsBySessionId(sessionId)
                : bookingRepository.findActiveBookingsBySessionId(sessionId);

        // ── 5. Map participants ───────────────────────────────────
        List<ParticipantResponse> participants = bookings.stream()
                .map(booking -> {
                    User user         = booking.getUser();
                    UserInfo userInfo = user.getUserInfo(); // nullable

                    return ParticipantResponse.builder()
                            .bookingId(booking.getId())
                            .bookingTime(booking.getBookingTime())
                            .userId(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .profilePhoto(user.getProfilePhoto())
                            .contactNumber(userInfo != null ? userInfo.getContactNumber() : null)
                            .address(userInfo != null ? userInfo.getAddress()             : null)
                            .motivation(userInfo != null ? userInfo.getMotivation()       : null)
                            .reason(userInfo != null ? userInfo.getReason()               : null)
                            .preferredSessionDuration(userInfo != null ? userInfo.getPreferredSessionDuration() : null)
                            .build();
                })
                .toList();

        // ── 6. Build and return response ──────────────────────────
        return SessionDetailResponse.builder()
                .sessionId(session.getId())
                .name(session.getName())
                .description(session.getDescription())
                .coachId(session.getCoach().getId())
                .coachName(session.getCoach().getFirstName() + " " + session.getCoach().getLastName())
                .startDay(session.getStartDay())
                .endDay(session.getEndDay())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .noOfSeats(session.getNoOfSeats())
                .metaData(session.getMetaData())
                .maxSeat(maxSeat)
                .occupiedSeats(occupiedSeats)
                .availableSeats(maxSeat - occupiedSeats)
                .participants(participants)
                .build();
    }

    @Override
    @Transactional
    public void deleteSession(Long requestingUserId, Long sessionId) {

        // ── 1. Get roles ──────────────────────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        // ── 2. Only COACH or ADMIN allowed ────────────────────────
        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to delete a session");
        }

        // ── 3. Fetch session ──────────────────────────────────────
        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // ── 4. Coach can only delete their own session ────────────
        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to delete this session");
        }

        // ── 5. Soft delete session ────────────────────────────────
        session.setIsDeleted(true);
        session.setIsActive(false);
        sessionTemplateRepository.save(session);

        // ── 6. Soft delete linked availability ───────────────────
        availabilityRepository.findBySessionId(sessionId).ifPresent(availability -> {
            availability.setIsDeleted(true);
            availability.setIsActive(false);
            availabilityRepository.save(availability);
        });
    }
}