package com.project.cbsbackend.dto.booking;


import lombok.Data;

@Data
public class UpdateBookingRequest {
    private Long sessionId;

    private Long participantId;
}
