package com.project.cbsbackend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String contactNumber;
    private String address;
    private String motivation;
    private String reason;
    private Integer preferredSessionDuration;
    private String bio;
}