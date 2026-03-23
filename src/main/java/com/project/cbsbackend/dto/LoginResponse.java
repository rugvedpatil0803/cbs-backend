package com.project.cbsbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class LoginResponse {
    private List<String> roles;
    private String token;
    private String refreshToken;   // ← add this
    private Long userId;
}