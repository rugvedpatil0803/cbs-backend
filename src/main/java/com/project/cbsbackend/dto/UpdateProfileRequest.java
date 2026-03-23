package com.project.cbsbackend.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String firstName;
    private String lastName;

    private String contactNumber;
    private String address;
    private String motivation;
    private String reason;
    private Integer preferredSessionDuration;
    private String bio;
}