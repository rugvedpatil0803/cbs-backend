package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.CreateFeedbackRequest;
import com.project.cbsbackend.dto.FeedbackResponse;
import com.project.cbsbackend.entity.Feedback;
import com.project.cbsbackend.entity.SessionTemplate;
import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.repository.*;
import com.project.cbsbackend.service.FeedbackService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final SessionTemplateRepository sessionTemplateRepository;
    private final UserRepository userRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public FeedbackResponse createFeedback(Long requestingUserId, CreateFeedbackRequest request) {

        // ── 1. Get roles of requesting user ───────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isParticipant = roles.contains("PARTICIPANT");
        boolean isAdmin       = roles.contains("ADMIN");

        // ── 2. Only PARTICIPANT or ADMIN allowed ──────────────────────
        if (!isParticipant && !isAdmin) {
            throw new RuntimeException("You are not allowed to submit feedback");
        }

        // ── 3. Validate request fields ────────────────────────────────
        if (request.getSessionId() == null) {
            throw new RuntimeException("Session ID is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        // ── 4. Fetch session ──────────────────────────────────────────
        SessionTemplate session = sessionTemplateRepository.findById(request.getSessionId())
                .filter(s -> !s.getIsDeleted() && s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        // ── 5. Fetch user ─────────────────────────────────────────────
        User user = userRepository.findById(requestingUserId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ── 6. Verify user has an active booking for this session ─────
        boolean hasBooked = bookingRepository
                .existsBySessionIdAndUserId(session.getId(), requestingUserId);

        if (!hasBooked && !isAdmin) {
            throw new RuntimeException("You can only give feedback for sessions you have booked");
        }

        // ── 7. Check duplicate feedback ───────────────────────────────
        boolean alreadyGivenFeedback = feedbackRepository
                .existsByUserIdAndSessionIdAndIsDeletedFalse(requestingUserId, session.getId());

        if (alreadyGivenFeedback) {
            throw new RuntimeException("You have already submitted feedback for this session");
        }

        // ── 8. Save feedback ──────────────────────────────────────────
        Feedback feedback = Feedback.builder()
                .user(user)
                .session(session)
                .rating(request.getRating())
                .feedbackDesc(request.getFeedbackDesc())
                .isActive(true)
                .isDeleted(false)
                .build();

        feedbackRepository.save(feedback);

        // ── 9. Return response ────────────────────────────────────────
        return mapToResponse(feedback);
    }

    @Override
    @Transactional
    public List<FeedbackResponse> getFeedbackBySession(Long sessionId) {

        // Verify session exists
        sessionTemplateRepository.findById(sessionId)
                .filter(s -> !s.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Session not found"));

        return feedbackRepository.findAllBySessionId(sessionId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<FeedbackResponse> getFeedbackByUser(Long requestingUserId, Long targetUserId) {

        // ── 1. Get roles ──────────────────────────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        // ── 2. Determine whose feedback to fetch ──────────────────────
        Long userId;
        if (isAdmin && targetUserId != null) {
            userId = targetUserId;
        } else {
            userId = requestingUserId;   // participants can only see their own
        }

        // ── 3. Verify user exists ─────────────────────────────────────
        userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return feedbackRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // ── Private helper ─────────────────────────────────────────────────────
    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getId())
                .sessionId(feedback.getSession().getId())
                .sessionName(feedback.getSession().getName())
                .userId(feedback.getUser().getId())
                .userName(feedback.getUser().getFirstName() + " " + feedback.getUser().getLastName())
                .rating(feedback.getRating())
                .feedbackDesc(feedback.getFeedbackDesc())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}