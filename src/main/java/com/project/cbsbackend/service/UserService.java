package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.UpdateProfileRequest;
import com.project.cbsbackend.dto.UpdateProfileResponse;

public interface UserService {
    UpdateProfileResponse updateProfile(Long requestingUserId, Long targetUserId, UpdateProfileRequest request);
}