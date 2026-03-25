package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.adminspecial.UserBookingListResponse;
import com.project.cbsbackend.dto.userprofile.UpdateProfileRequest;
import com.project.cbsbackend.dto.userprofile.UpdateProfileResponse;
import com.project.cbsbackend.dto.adminspecial.UserSummaryResponse;

import java.util.List;
import java.util.Map;

public interface UserService {
    UpdateProfileResponse updateProfile(Long requestingUserId, Long targetUserId, UpdateProfileRequest request);
    UpdateProfileResponse getProfile(Long requestingUserId, Long targetUserId);
    Map<String, List<UserSummaryResponse>> getUsersGroupedByRole(Long requestingUserId);
    void deactivateUser(Long requestingUserId, Long targetUserId);
    List<UserBookingListResponse> getUserBookings(Long requestingUserId, Long targetUserId);



}