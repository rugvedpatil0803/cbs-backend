package com.project.cbsbackend.dto.booking;

import lombok.Data;

@Data
public class UpdateBookingResponse {
    private Long bookingId;
    private Long sessionId;
    private String sessionName;
    private Long participantId;
    private String participantName;
    private Long coachId;
    private String coachName;
}
