package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.CreateSessionRequest;
import com.project.cbsbackend.dto.CreateSessionResponse;
import com.project.cbsbackend.dto.SessionWithAvailabilityResponse;
import com.project.cbsbackend.dto.UpdateSessionRequest;

import java.util.List;


public interface SessionService {
    CreateSessionResponse createSession(Long requestingUserId, CreateSessionRequest request);
    CreateSessionResponse updateSession(Long requestingUserId, Long sessionId, UpdateSessionRequest request);

    List<SessionWithAvailabilityResponse> getUpcomingSessions();
    List<SessionWithAvailabilityResponse> getOngoingSessions();
    List<SessionWithAvailabilityResponse> getCompletedSessions();
}