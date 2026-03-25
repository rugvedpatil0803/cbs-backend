package com.project.cbsbackend.dto.booking;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CreateBookingResponse {
    private Long bookingId;
    private Long sessionId;
    private String sessionName;
    private Long participantId;
    private String participantName;
    private Long coachId;
    private String coachName;
    private Long availabilityId;
    private Integer remainingSeats;
    private LocalDateTime bookingTime;
}