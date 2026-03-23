package com.project.cbsbackend.dto;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private Long sessionId;

    // Only used when Admin assigns participant to a session
    private Long participantId;
}