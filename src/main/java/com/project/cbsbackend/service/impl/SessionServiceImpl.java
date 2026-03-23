package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.CreateSessionRequest;
import com.project.cbsbackend.dto.CreateSessionResponse;
import com.project.cbsbackend.dto.UpdateSessionRequest;
import com.project.cbsbackend.entity.SessionTemplate;
import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.repository.SessionTemplateRepository;
import com.project.cbsbackend.repository.UserRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;
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

        // ── 8. Return response ────────────────────────────────────────
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

        // ── 6. Partial update — only update non-null fields ───────────
        if (request.getName()        != null) session.setName(request.getName());
        if (request.getDescription() != null) session.setDescription(request.getDescription());
        if (request.getStartDay()    != null) session.setStartDay(request.getStartDay());
        if (request.getEndDay()      != null) session.setEndDay(request.getEndDay());
        if (request.getStartTime()   != null) session.setStartTime(request.getStartTime());
        if (request.getEndTime()     != null) session.setEndTime(request.getEndTime());
        if (request.getNoOfSeats()   != null) session.setNoOfSeats(request.getNoOfSeats());
        if (request.getMetaData()    != null) session.setMetaData(request.getMetaData());

        sessionTemplateRepository.save(session);

        // ── 7. Return response ────────────────────────────────────────
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
}