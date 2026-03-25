package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.feedback.CreateFeedbackRequest;
import com.project.cbsbackend.dto.feedback.FeedbackResponse;

import java.util.List;

public interface FeedbackService {
    FeedbackResponse createFeedback(Long requestingUserId, CreateFeedbackRequest request);
    List<FeedbackResponse> getFeedbackBySession(Long sessionId);
    List<FeedbackResponse> getFeedbackByUser(Long requestingUserId, Long targetUserId);
}