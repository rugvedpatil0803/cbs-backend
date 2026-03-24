package com.project.cbsbackend.controller;

import com.project.cbsbackend.dto.*;
import com.project.cbsbackend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> register(@Valid @RequestBody RegisterRequest request){
        try {
            authService.registerUser(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.builder()
                            .status("success")
                            .message("User registered successfully")
                            .data(null)
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

    @PostMapping("/login/email")
    public ResponseEntity<ApiResponse<?>> checkEmail(@RequestBody EmailCheckRequest request) {

        authService.checkEmail(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .message("Email verified")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .message("Login successful")
                        .data(response)
                        .build()
        );
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<?>> requestOtp(@RequestBody ForgotPasswordRequest request) {

        authService.requestOtp(request.getEmail());

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .message("OTP has been sent to your email")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(request);

        return ResponseEntity.ok(
                ApiResponse.builder()
                        .status("success")
                        .message("Password updated successfully")
                        .data(null)
                        .build()
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refresh(@RequestBody RefreshTokenRequest request) {
        try {
            LoginResponse data = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .status("success")
                            .message("Token refreshed")
                            .data(data)
                            .build()
            );
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.builder()
                            .status("error")
                            .message(ex.getMessage())
                            .data(null)
                            .build()
                    );
        }
    }
}