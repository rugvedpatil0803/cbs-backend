package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.adminspecial.*;
import com.project.cbsbackend.dto.session.*;
import com.project.cbsbackend.entity.*;
import com.project.cbsbackend.repository.*;
import com.project.cbsbackend.service.SessionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

    private final SessionTemplateRepository sessionTemplateRepository;
    private final UserRepository userRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final AvailabilityRepository availabilityRepository;
    private final BookingRepository bookingRepository;
    private final FeedbackRepository feedbackRepository;

    @Override
    @Transactional
    public CreateSessionResponse createSession(Long requestingUserId, CreateSessionRequest request) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to create a session");
        }

        Long coachId;
        if (isAdmin && request.getCoachId() != null) {
            coachId = request.getCoachId();
        } else {
            coachId = requestingUserId;
        }

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

        User coach = userRepository.findById(coachId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("Coach not found"));

        List<String> coachRoles = userRoleLinkRepository.findByUserId(coachId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!coachRoles.contains("COACH")) {
            throw new RuntimeException("Target user is not a coach");
        }

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

        Availability availability = Availability.builder()
                .session(session)
                .maxSeat(request.getNoOfSeats())
                .occupiedSeats(0)
                .isActive(true)
                .isDeleted(false)
                .build();

        availabilityRepository.save(availability);

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to update a session");
        }

        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to update another coach's session");
        }

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

        if (request.getName()        != null) session.setName(request.getName());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getStartDay()    != null) session.setStartDay(request.getStartDay());
        if (request.getEndDay()      != null) session.setEndDay(request.getEndDay());
        if (request.getStartTime()   != null) session.setStartTime(request.getStartTime());
        if (request.getEndTime()     != null) session.setEndTime(request.getEndTime());
        if (request.getNoOfSeats()   != null) session.setNoOfSeats(request.getNoOfSeats());
        if (request.getMetaData()    != null) session.setMetaData(request.getMetaData());

        sessionTemplateRepository.save(session);

        if (request.getNoOfSeats() != null) {
            Availability availability = availabilityRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Availability not found for this session"));

            if (request.getNoOfSeats() < availability.getOccupiedSeats()) {
                throw new RuntimeException("New seat count cannot be less than already occupied seats ("
                        + availability.getOccupiedSeats() + ")");
            }

            availability.setMaxSeat(request.getNoOfSeats());
            availabilityRepository.save(availability);
        }

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
    @Transactional
    public List<SessionWithAvailabilityResponse> getUpcomingSessions() {
        return sessionTemplateRepository.findUpcomingSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional
    public List<SessionWithAvailabilityResponse> getOngoingSessions() {
        return sessionTemplateRepository.findOngoingSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional
    public List<SessionWithAvailabilityResponse> getCompletedSessions() {
        return sessionTemplateRepository.findCompletedSessions(LocalDate.now())
                .stream()
                .map(this::mapToSessionWithAvailability)
                .toList();
    }

    @Override
    @Transactional
    public MySessionsResponse getMySessions(Long requestingUserId) {

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

        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to view this session");
        }

        Availability availability = availabilityRepository.findBySessionId(sessionId)
                .orElse(null);

        int maxSeat       = availability != null ? availability.getMaxSeat()       : 0;
        int occupiedSeats = availability != null ? availability.getOccupiedSeats() : 0;

        List<Booking> bookings = isAdmin
                ? bookingRepository.findAllBookingsBySessionId(sessionId)
                : bookingRepository.findActiveBookingsBySessionId(sessionId);

        List<ParticipantResponse> participants = bookings.stream()
                .map(booking -> {
                    User user         = booking.getUser();
                    UserInfo userInfo = user.getUserInfo();

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to delete a session");
        }

        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to delete this session");
        }

        session.setIsDeleted(true);
        session.setIsActive(false);
        sessionTemplateRepository.save(session);

        availabilityRepository.findBySessionId(sessionId).ifPresent(availability -> {
            availability.setIsDeleted(true);
            availability.setIsActive(false);
            availabilityRepository.save(availability);
        });
    }

    @Override
    @Transactional
    public SessionAnalyticsResponse getSessionAnalytics(Long requestingUserId, Long sessionId) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");
        boolean isCoach = roles.contains("COACH");

        if (!isAdmin && !isCoach) {
            throw new RuntimeException("You are not allowed to view session analytics");
        }

        SessionTemplate session = sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!isAdmin && !session.getCoach().getId().equals(requestingUserId)) {
            throw new RuntimeException("You are not allowed to view this session analytics");
        }

        Availability availability = availabilityRepository.findBySessionId(sessionId)
                .orElse(null);

        int maxSeat = availability != null && availability.getMaxSeat() != null ? availability.getMaxSeat() : 0;
        int occupiedSeats = availability != null && availability.getOccupiedSeats() != null ? availability.getOccupiedSeats() : 0;
        int availableSeats = Math.max(maxSeat - occupiedSeats, 0);

        BigDecimal occupancyPercentage = BigDecimal.ZERO;
        if (maxSeat > 0) {
            occupancyPercentage = BigDecimal.valueOf((occupiedSeats * 100.0) / maxSeat)
                    .setScale(2, java.math.RoundingMode.HALF_UP);
        }

        long totalBookings = bookingRepository.countBySessionIdAndIsDeletedFalse(sessionId);
        long activeBookings = bookingRepository.countBySessionIdAndIsActiveTrueAndIsDeletedFalse(sessionId);
        long cancelledBookings = bookingRepository.countBySessionIdAndIsActiveFalseAndIsDeletedFalse(sessionId);
        long deletedBookings = bookingRepository.countBySessionIdAndIsDeletedTrue(sessionId);

        List<Booking> bookings = isAdmin
                ? bookingRepository.findAllValidBookingsBySessionId(sessionId)
                : bookingRepository.findActiveBookingsBySessionId(sessionId);

        List<SessionAnalyticsResponse.ParticipantItem> participants = bookings.stream()
                .filter(b -> Boolean.TRUE.equals(b.getIsActive()) && !Boolean.TRUE.equals(b.getIsDeleted()))
                .map(booking -> {
                    User user = booking.getUser();
                    UserInfo userInfo = user.getUserInfo();

                    return SessionAnalyticsResponse.ParticipantItem.builder()
                            .userId(user.getId())
                            .name(user.getFirstName() + " " + user.getLastName())
                            .email(user.getEmail())
                            .contactNumber(userInfo != null ? userInfo.getContactNumber() : null)
                            .bookingTime(booking.getBookingTime())
                            .build();
                })
                .toList();

        Double avgRating = feedbackRepository.findAverageRatingBySessionId(sessionId);
        BigDecimal averageRating = avgRating != null
                ? BigDecimal.valueOf(avgRating).setScale(1, java.math.RoundingMode.HALF_UP)
                : null;


        String status = resolveSessionStatus(session);


        return SessionAnalyticsResponse.builder()
                .session(SessionAnalyticsResponse.SessionInfo.builder()
                        .sessionId(session.getId())
                        .name(session.getName())
                        .description(session.getDescription())
                        .coach(session.getCoach().getFirstName() + " " + session.getCoach().getLastName())
                        .schedule(SessionAnalyticsResponse.ScheduleInfo.builder()
                                .startDay(session.getStartDay())
                                .endDay(session.getEndDay())
                                .startTime(session.getStartTime())
                                .endTime(session.getEndTime())
                                .build())
                        .totalSeats(session.getNoOfSeats())
                        .build())
                .availability(SessionAnalyticsResponse.AvailabilityInfo.builder()
                        .maxSeat(maxSeat)
                        .occupiedSeats(occupiedSeats)
                        .availableSeats(availableSeats)
                        .occupancyPercentage(occupancyPercentage)
                        .build())
                .bookingStats(SessionAnalyticsResponse.BookingStats.builder()
                        .totalBookings(totalBookings)
                        .activeBookings(activeBookings)
                        .cancelledBookings(cancelledBookings)
                        .deletedBookings(deletedBookings)
                        .build())
                .participants(SessionAnalyticsResponse.ParticipantsInfo.builder()
                        .totalParticipants((long) participants.size())
                        .allParticipants(participants)
                        .build())
                .feedbackStats(SessionAnalyticsResponse.FeedbackStats.builder()
                        .averageRating(averageRating)
                        .build())
                .status(status)
                .build();
    }
    private String resolveSessionStatus(SessionTemplate session) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (session.getStartDay() != null && today.isBefore(session.getStartDay())) {
            return "UPCOMING";
        }

        if (session.getEndDay() != null && today.isAfter(session.getEndDay())) {
            return "COMPLETED";
        }

        if (session.getStartDay() != null && session.getEndDay() != null) {
            if (today.isEqual(session.getStartDay()) && session.getStartTime() != null && now.isBefore(session.getStartTime())) {
                return "UPCOMING";
            }

            if (today.isEqual(session.getEndDay()) && session.getEndTime() != null && now.isAfter(session.getEndTime())) {
                return "COMPLETED";
            }
        }

        return "ONGOING";
    }
}