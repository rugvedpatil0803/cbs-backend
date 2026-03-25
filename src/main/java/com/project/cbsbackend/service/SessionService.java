package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.*;

import java.util.List;


public interface SessionService {
    CreateSessionResponse createSession(Long requestingUserId, CreateSessionRequest request);
    CreateSessionResponse updateSession(Long requestingUserId, Long sessionId, UpdateSessionRequest request);

    List<SessionWithAvailabilityResponse> getUpcomingSessions();
    List<SessionWithAvailabilityResponse> getOngoingSessions();
    List<SessionWithAvailabilityResponse> getCompletedSessions();

    MySessionsResponse getMySessions(Long coachId);
    SessionDetailResponse getSessionDetails(Long requestingUserId, Long sessionId, boolean isAdmin);
    void deleteSession(Long requestingUserId, Long sessionId);


}