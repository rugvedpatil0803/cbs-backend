package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.feedback.CreateFeedbackRequest;
import com.project.cbsbackend.dto.feedback.FeedbackResponse;
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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isParticipant = roles.contains("PARTICIPANT");
        boolean isAdmin       = roles.contains("ADMIN");

        if (!isParticipant && !isAdmin) {
            throw new RuntimeException("You are not allowed to submit feedback");
        }

        if (request.getSessionId() == null) {
            throw new RuntimeException("Session ID is required");
        }
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new RuntimeException("Rating must be between 1 and 5");
        }

        SessionTemplate session = sessionTemplateRepository.findById(request.getSessionId())
                .filter(s -> !s.getIsDeleted() && s.getIsActive())
                .orElseThrow(() -> new RuntimeException("Session not found or inactive"));

        User user = userRepository.findById(requestingUserId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean hasBooked = bookingRepository
                .existsBySessionIdAndUserId(session.getId(), requestingUserId);

        if (!hasBooked && !isAdmin) {
            throw new RuntimeException("You can only give feedback for sessions you have booked");
        }

        boolean alreadyGivenFeedback = feedbackRepository
                .existsByUserIdAndSessionIdAndIsDeletedFalse(requestingUserId, session.getId());

        if (alreadyGivenFeedback) {
            throw new RuntimeException("You have already submitted feedback for this session");
        }

        Feedback feedback = Feedback.builder()
                .user(user)
                .session(session)
                .rating(request.getRating())
                .feedbackDesc(request.getFeedbackDesc())
                .isActive(true)
                .isDeleted(false)
                .build();

        feedbackRepository.save(feedback);

        return mapToResponse(feedback);
    }

    @Override
    @Transactional
    public List<FeedbackResponse> getFeedbackBySession(Long sessionId) {

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        Long userId;
        if (isAdmin && targetUserId != null) {
            userId = targetUserId;
        } else {
            userId = requestingUserId;
        }

        userRepository.findById(userId)
                .filter(u -> !u.getIsDeleted() && u.getIsActive())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return feedbackRepository.findAllByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

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