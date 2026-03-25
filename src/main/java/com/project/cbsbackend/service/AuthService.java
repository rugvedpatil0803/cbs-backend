package com.project.cbsbackend.service;

import com.project.cbsbackend.dto.login.LoginRequest;
import com.project.cbsbackend.dto.login.LoginResponse;
import com.project.cbsbackend.dto.userprofile.RegisterRequest;
import com.project.cbsbackend.dto.resetpass.ResetPasswordRequest;


public interface AuthService {
    void registerUser(RegisterRequest request);
    void checkEmail(String email);

    LoginResponse login(LoginRequest request);

    void requestOtp(String email);

    void resetPassword(ResetPasswordRequest request);

    LoginResponse refreshToken(String refreshToken);
}

