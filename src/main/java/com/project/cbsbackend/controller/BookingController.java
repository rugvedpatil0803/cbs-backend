package com.project.cbsbackend.controller;

import com.project.cbsbackend.config.JwtUtil;
import com.project.cbsbackend.dto.ApiResponse;
import com.project.cbsbackend.dto.CreateBookingRequest;
import com.project.cbsbackend.dto.CreateBookingResponse;
import com.project.cbsbackend.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final JwtUtil jwtUtil;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<?>> createBooking(
            @RequestBody CreateBookingRequest request,
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            CreateBookingResponse data = bookingService.createBooking(requestingUserId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .status("success")
                            .message("Booking created successfully")
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

    @DeleteMapping("/unenroll/{sessionId}")
    public ResponseEntity<ApiResponse<?>> unenrollBooking(
            @PathVariable Long sessionId,
            @RequestParam(required = false) Long participantId,  // only for admin
            HttpServletRequest httpRequest) {
        try {
            String token = httpRequest.getHeader("Authorization").substring(7);
            Long requestingUserId = jwtUtil.extractUserId(token);

            bookingService.unenrollBooking(requestingUserId, sessionId, participantId);

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Booking cancelled successfully")
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