package com.project.cbsbackend.service.impl;

import com.project.cbsbackend.config.JwtUtil;
import com.project.cbsbackend.dto.LoginRequest;
import com.project.cbsbackend.dto.LoginResponse;
import com.project.cbsbackend.dto.RegisterRequest;
import com.project.cbsbackend.dto.ResetPasswordRequest;
import com.project.cbsbackend.entity.Role;
import com.project.cbsbackend.entity.User;
import com.project.cbsbackend.entity.UserInfo;
import com.project.cbsbackend.entity.UserRoleLink;
import com.project.cbsbackend.repository.RoleRepository;
import com.project.cbsbackend.repository.UserInfoRepository;
import com.project.cbsbackend.repository.UserRepository;
import com.project.cbsbackend.repository.UserRoleLinkRepository;
import com.project.cbsbackend.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserRoleLinkRepository userRoleLinkRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void registerUser(RegisterRequest dto) {

        // 0. Basic validation
        if (dto.getEmail() == null || dto.getPassword() == null || dto.getRole() == null) {
            throw new RuntimeException("Email, Password and Role are required");
        }

        // 1. Check email exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // 2. Fetch Role (safe handling)
        Role role = roleRepository.findByRoleName(dto.getRole().trim().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Invalid role"));

        // 3. Create User
        User user = User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .isActive(true)
                .isDeleted(false)
                .isSuperuser("ADMIN".equalsIgnoreCase(role.getRoleName())) // auto set if admin
                .build();

        userRepository.save(user);

        // 4. Create UserInfo
        UserInfo userInfo = UserInfo.builder()
                .user(user)
                .contactNumber(dto.getContactNumber())
                .address(dto.getAddress())
                .motivation(dto.getMotivation())
                .reason(dto.getReason())
                .preferredSessionDuration(dto.getPreferredSessionDuration())
                .bio(dto.getBio())
                .isActive(true)
                .isDeleted(false)
                .build();

        userInfoRepository.save(userInfo);

        // 5. Create UserRoleLink
        UserRoleLink link = UserRoleLink.builder()
                .user(user)
                .role(role)
                .isActive(true)
                .isDeleted(false)
                .build();

        userRoleLinkRepository.save(link);
    }

    @Override
    public void checkEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        userRepository.findByEmail(email)
                .filter(user -> !user.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Email not registered"));
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        List<String> roles = userRoleLinkRepository.findByUser(user)
                .stream()
                .map(link -> link.getRole().getRoleName())
                .toList();

        String token        = jwtUtil.generateToken(user.getId(), user.getEmail(), roles);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), roles);

        return LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .roles(roles)
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    public void requestOtp(String email) {

        // Reuse your existing method
        checkEmail(email); // throws if invalid

        // Static OTP
        String otp = "123456";

        // For testing (since no email service)
        System.out.println("OTP for " + email + ": " + otp);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        // 1. Validate email
        User user = userRepository.findByEmail(request.getEmail())
                .filter(u -> !u.getIsDeleted())
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!user.getIsActive()) {
            throw new RuntimeException("User is inactive");
        }

        // 2. Validate OTP (static)
        if (!"123456".equals(request.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // 3. Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {

        if (!jwtUtil.isTokenValid(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String email       = jwtUtil.extractEmail(refreshToken);
        Long userId        = jwtUtil.extractUserId(refreshToken);
        List<String> roles = jwtUtil.extractRoles(refreshToken);

        String newAccessToken  = jwtUtil.generateToken(userId, email, roles);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, email, roles);

        return LoginResponse.builder()
                .token(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(userId)
                .roles(roles)
                .build();
    }

}