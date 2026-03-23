package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.CreateSessionRequest;
import com.project.cbsbackend.dto.CreateSessionResponse;
import com.project.cbsbackend.dto.UpdateSessionRequest;


public interface SessionService {
    CreateSessionResponse createSession(Long requestingUserId, CreateSessionRequest request);
    CreateSessionResponse updateSession(Long requestingUserId, Long sessionId, UpdateSessionRequest request);
}