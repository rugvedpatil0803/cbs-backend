package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.dto.adminspecial.UserBookingListResponse;
import com.project.cbsbackend.dto.userprofile.UpdateProfileRequest;
import com.project.cbsbackend.dto.userprofile.UpdateProfileResponse;
import com.project.cbsbackend.dto.adminspecial.UserSummaryResponse;
import com.project.cbsbackend.entity.*;
import com.project.cbsbackend.repository.BookingRepository;
import com.project.cbsbackend.repository.UserInfoRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;  // ← ADD
import com.project.cbsbackend.repository.UserRepository;
import com.project.cbsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.format.DateTimeFormatter;
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
    private final BookingRepository bookingRepository;


    @Override
    @Transactional
    public UpdateProfileResponse updateProfile(Long requestingUserId, Long targetUserId, UpdateProfileRequest request) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new RuntimeException("You are not allowed to update another user's profile");
        }

        User user = userRepository.findById(targetUserId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User is inactive");
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) user.setLastName(request.getLastName());

        userRepository.save(user);

        UserInfo userInfo = userInfoRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User info not found"));

        if (request.getContactNumber()            != null) userInfo.setContactNumber(request.getContactNumber());
        if (request.getAddress()                  != null) userInfo.setAddress(request.getAddress());
        if (request.getMotivation()               != null) userInfo.setMotivation(request.getMotivation());
        if (request.getReason()                   != null) userInfo.setReason(request.getReason());
        if (request.getPreferredSessionDuration() != null) userInfo.setPreferredSessionDuration(request.getPreferredSessionDuration());
        if (request.getBio()                      != null) userInfo.setBio(request.getBio());

        userInfoRepository.save(userInfo);

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new RuntimeException("You are not allowed to view another user's profile");
        }

        User user = userRepository.findById(targetUserId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));


        UserInfo userInfo = userInfoRepository.findByUserId(targetUserId)
                .orElseThrow(() -> new RuntimeException("User info not found"));

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

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!roles.contains("ADMIN")) {
            throw new RuntimeException("You are not allowed to access this resource");
        }

        List<UserRoleLink> activeRoleLinks = userRoleLinkRepository.findAllActiveRoleLinks();

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

    @Override
    @Transactional
    public void deactivateUser(Long requestingUserId, Long targetUserId) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        if (!roles.contains("ADMIN")) {
            throw new RuntimeException("You are not allowed to perform this action");
        }

        User targetUser = userRepository.findById(targetUserId)
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean currentStatus = targetUser.getIsActive();
        targetUser.setIsActive(!currentStatus);

        userRepository.save(targetUser);
    }

    @Override
    public List<UserBookingListResponse> getUserBookings(Long requestingUserId, Long targetUserId) {

        List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        boolean isAdmin = roles.contains("ADMIN");

        if (!isAdmin && !requestingUserId.equals(targetUserId)) {
            throw new RuntimeException("You are not allowed to view these bookings");
        }

        userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingRepository.findAllBookingsByUserId(targetUserId);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        DateTimeFormatter dateFormatter     = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter timeFormatter     = DateTimeFormatter.ofPattern("hh:mm a");

        return bookings.stream()
                .map(booking -> {
                    SessionTemplate session = booking.getSession();
                    User coach = session.getCoach();

                    return UserBookingListResponse.builder()
                            .bookingId(booking.getId())
                            .bookingTime(booking.getBookingTime() != null
                                    ? booking.getBookingTime().format(dateTimeFormatter) : null)
                            .sessionId(session.getId())
                            .sessionName(session.getName())
                            .sessionDescription(session.getDescription())
                            .startDay(session.getStartDay() != null
                                    ? session.getStartDay().format(dateFormatter) : null)
                            .endDay(session.getEndDay() != null
                                    ? session.getEndDay().format(dateFormatter) : null)
                            .startTime(session.getStartTime() != null
                                    ? session.getStartTime().format(timeFormatter) : null)
                            .endTime(session.getEndTime() != null
                                    ? session.getEndTime().format(timeFormatter) : null)
                            .metaData(session.getMetaData())
                            .coachId(coach.getId())
                            .coachName(coach.getFirstName() + " " + coach.getLastName())
                            .isActive(booking.getIsActive())
                            .isDeleted(booking.getIsDeleted())
                            .build();
                })
                .toList();
    }
}