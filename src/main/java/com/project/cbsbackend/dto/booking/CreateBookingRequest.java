package com.project.cbsbackend.dto.booking;

import lombok.Data;

@Data
public class CreateBookingRequest {
    private Long sessionId;

    private Long participantId;
}