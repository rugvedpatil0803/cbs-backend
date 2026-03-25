package com.project.cbsbackend.controller;

import com.project.cbsbackend.dto.ApiResponse;
import com.project.cbsbackend.dto.UpdateProfileRequest;
import com.project.cbsbackend.dto.UpdateProfileResponse;
import com.project.cbsbackend.dto.UserSummaryResponse;
import com.project.cbsbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.cbsbackend.config.JwtUtil;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @PathVariable Long userId,
            @RequestBody UpdateProfileRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            // Get requesting user's ID from token
            String authHeader = httpRequest.getHeader("Authorization");
            String token = authHeader.substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            UpdateProfileResponse data = userService.updateProfile(requestingUserId, userId, request);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Profile updated successfully")
                            .data(data)
                            .build()
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
                            .build()
                    );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build()
                    );
        }
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<?>> getProfile(
            @PathVariable Long userId,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            // Get requesting user's ID from token
            String authHeader = httpRequest.getHeader("Authorization");
            String token = authHeader.substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            UpdateProfileResponse data = userService.getProfile(requestingUserId, userId);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Profile fetched successfully")
                            .data(data)
                            .build()
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
                            .build()
                    );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build()
                    );
        }
    }

    @GetMapping("/list-by-roles")
    public ResponseEntity<ApiResponse<?>> getUsersGroupedByRole(
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            Map<String, List<UserSummaryResponse>> data = userService.getUsersGroupedByRole(requestingUserId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Users fetched successfully")
                            .data(data)
                            .build()
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
                            .build()
                    );
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build()
                    );
        }
    }
}