package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.LoginRequest;
import com.project.cbsbackend.dto.LoginResponse;
import com.project.cbsbackend.dto.RegisterRequest;
import com.project.cbsbackend.dto.ResetPasswordRequest;


public interface AuthService {
    void registerUser(RegisterRequest request);
    void checkEmail(String email);

    LoginResponse login(LoginRequest request);

    void requestOtp(String email);

    void resetPassword(ResetPasswordRequest request);

    LoginResponse refreshToken(String refreshToken);
}

