package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.UpdateProfileRequest;
import com.project.cbsbackend.dto.UpdateProfileResponse;
import com.project.cbsbackend.dto.UserSummaryResponse;

import java.util.List;
import java.util.Map;

public interface UserService {
    UpdateProfileResponse updateProfile(Long requestingUserId, Long targetUserId, UpdateProfileRequest request);
    UpdateProfileResponse getProfile(Long requestingUserId, Long targetUserId);
    Map<String, List<UserSummaryResponse>> getUsersGroupedByRole(Long requestingUserId);

}