package com.project.cbsbackend.controller;

import com.project.cbsbackend.config.JwtUtil;
import com.project.cbsbackend.dto.ApiResponse;
import com.project.cbsbackend.dto.CreateFeedbackRequest;
import com.project.cbsbackend.dto.FeedbackResponse;
import com.project.cbsbackend.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createFeedback(
            @RequestBody CreateFeedbackRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            FeedbackResponse data = feedbackService.createFeedback(requestingUserId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .status("success")
                            .message("Feedback submitted successfully")
                            .data(data)
                            .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
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

    // Get all feedback for a particular session — open to ADMIN and COACH
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResponse<?>> getFeedbackBySession(
            @PathVariable Long sessionId,
            HttpServletRequest httpRequest) {
        try {
            List<FeedbackResponse> data = feedbackService.getFeedbackBySession(sessionId);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .message("Feedback fetched successfully")
                    .data(data)
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
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

    // Get all feedback by a particular user — participant sees own, admin can pass ?userId=
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<?>> getFeedbackByUser(
            @RequestParam(required = false) Long userId,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            List<FeedbackResponse> data = feedbackService.getFeedbackByUser(requestingUserId, userId);

            return ResponseEntity.ok(ApiResponse.builder()
                    .status("success")
                    .message("Feedback fetched successfully")
                    .data(data)
                    .build());
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
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
}