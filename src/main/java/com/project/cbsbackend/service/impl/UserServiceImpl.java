package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.UpdateProfileRequest;
import com.project.cbsbackend.dto.UpdateProfileResponse;
import com.project.cbsbackend.dto.UserSummaryResponse;
import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.entity.UserInfo;
import com.project.cbsbackend.entity.UserRoleLink;
import com.project.cbsbackend.repository.UserInfoRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;  // ← ADD
import com.project.cbsbackend.repository.UserRepository;
import com.project.cbsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;

    @Override
    @Transactional
    public UpdateProfileResponse updateProfile(Long requestingUserId, Long targetUserId, UpdateProfileRequest request) {

        // ── 1. Security check ─────────────────────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new RuntimeException("You are not allowed to update another user's profile");
        }

        // ── 2. Fetch target user ──────────────────────────────────────
        User user = userRepository.findById(targetUserId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User is inactive");
        }

        // ── 3. Update tbl_user fields ─────────────────────────────────
        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());

        userRepository.save(user);

        // ── 4. Update tbl_user_info fields ────────────────────────────
        UserInfo userInfo = userInfoRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User info not found"));

        if (request.getContactNumber()            != null) userInfo.setContactNumber(request.getContactNumber());
        if (request.getAddress()                  != null) userInfo.setAddress(request.getAddress());
        if (request.getMotivation()               != null) userInfo.setMotivation(request.getMotivation());
        if (request.getReason()                   != null) userInfo.setReason(request.getReason());
        if (request.getPreferredSessionDuration() != null) userInfo.setPreferredSessionDuration(request.getPreferredSessionDuration());
        if (request.getBio()                      != null) userInfo.setBio(request.getBio());

        userInfoRepository.save(userInfo);

        // ── 5. Return response ────────────────────────────────────────
        return UpdateProfileResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(userInfo.getContactNumber())
                .address(userInfo.getAddress())
                .motivation(userInfo.getMotivation())
                .reason(userInfo.getReason())
                .preferredSessionDuration(userInfo.getPreferredSessionDuration())
                .bio(userInfo.getBio())
                .build();
    }

    @Override
    public UpdateProfileResponse getProfile(Long requestingUserId, Long targetUserId) {

        // ── 1. Security check ─────────────────────────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new RuntimeException("You are not allowed to view another user's profile");
        }

        // ── 2. Fetch target user ──────────────────────────────────────
        User user = userRepository.findById(targetUserId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User is inactive");
        }

        // ── 3. Fetch user info ────────────────────────────────────────
        UserInfo userInfo = userInfoRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User info not found"));

        // ── 4. Return response ────────────────────────────────────────
        return UpdateProfileResponse.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .contactNumber(userInfo.getContactNumber())
                .address(userInfo.getAddress())
                .motivation(userInfo.getMotivation())
                .reason(userInfo.getReason())
                .preferredSessionDuration(userInfo.getPreferredSessionDuration())
                .bio(userInfo.getBio())
                .build();
    }

    @Override
    public Map<String, List<UserSummaryResponse>> getUsersGroupedByRole(Long requestingUserId) {

        // ── 1. Verify requesting user is ADMIN ────────────────────
        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!roles.contains("ADMIN")) {
            throw new RuntimeException("You are not allowed to access this resource");
        }

        // ── 2. Fetch all active role links ────────────────────────
        List<UserRoleLink> activeRoleLinks = userRoleLinkRepository.findAllActiveRoleLinks();

        // ── 3. Group by role name ─────────────────────────────────
        Map<String, List<UserSummaryResponse>> result = new LinkedHashMap<>();

        for (UserRoleLink link : activeRoleLinks) {
            String roleName = link.getRole().getRoleName();
            User user = link.getUser();

            UserSummaryResponse userSummary = UserSummaryResponse.builder()
                    .userId(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .isActive(user.getIsActive())
                    .build();

            result.computeIfAbsent(roleName, k -> new ArrayList<>()).add(userSummary);
        }

        return result;
    }
}