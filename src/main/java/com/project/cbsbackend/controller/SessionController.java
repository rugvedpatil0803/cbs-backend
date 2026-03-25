package com.project.cbsbackend.controller;

import com.project.cbsbackend.config.JwtUtil;
import com.project.cbsbackend.dto.*;
import com.project.cbsbackend.dto.session.*;
import com.project.cbsbackend.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.project.cbsbackend.repository.UserRoleLinkRepository;

import java.util.List;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;
    private final UserRoleLinkRepository userRoleLinkRepository;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createSession(
            @RequestBody CreateSessionRequest request,
            HttpServletRequest httpRequest) {
        try {
            // Get requesting userId from token
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            CreateSessionResponse data = sessionService.createSession(requestingUserId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .status("success")
                            .message("Session created successfully")
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

    @PutMapping("/update/{sessionId}")
    public ResponseEntity<ApiResponse<?>> updateSession(
            @PathVariable Long sessionId,
            @RequestBody UpdateSessionRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            CreateSessionResponse data = sessionService.updateSession(requestingUserId, sessionId, request);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Session updated successfully")
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

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<?>> getUpcomingSessions() {
        try {
            List<SessionWithAvailabilityResponse> data = sessionService.getUpcomingSessions();
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .message("Upcoming sessions fetched successfully")
                    .data(data)
                    .build());
        } catch (Exception ex) {
            System.out.println("ERROR: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/ongoing")
    public ResponseEntity<ApiResponse<?>> getOngoingSessions() {
        try {
            List<SessionWithAvailabilityResponse> data = sessionService.getOngoingSessions();
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .message("Ongoing sessions fetched successfully")
                    .data(data)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<?>> getCompletedSessions() {
        try {
            List<SessionWithAvailabilityResponse> data = sessionService.getCompletedSessions();
            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .message("Completed sessions fetched successfully")
                    .data(data)
                    .build());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message("Something went wrong")
                            .data(null)
                            .build());
        }
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<ApiResponse<?>> getMySessions(HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            MySessionsResponse data = sessionService.getMySessions(requestingUserId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("My sessions fetched successfully")
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

    @GetMapping("/details/{sessionId}")
    public ResponseEntity<ApiResponse<?>> getSessionDetails(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            // Resolve roles once here and pass isAdmin flag down
            List<String> roles = userRoleLinkRepository.findByUserId(requestingUserId)
                    .stream()
                    .map(link -> link.getRole().getRoleName())
                    .toList();

            boolean isAdmin = roles.contains("ADMIN");
            boolean isCoach = roles.contains("COACH");

            if (!isAdmin && !isCoach) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.builder()
                                .status("error")
                                .message("You are not allowed to view session details")
                                .data(null)
                                .build()
                        );
            }

            SessionDetailResponse data = sessionService.getSessionDetails(requestingUserId, sessionId, isAdmin);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Session details fetched successfully")
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

    @DeleteMapping("/delete/{sessionId}")
    public ResponseEntity<ApiResponse<?>> deleteSession(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            sessionService.deleteSession(requestingUserId, sessionId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Session deleted successfully")
                            .data(null)
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
}