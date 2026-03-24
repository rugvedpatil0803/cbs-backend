package com.project.cbsbackend.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookingResponse {
    private Long bookingId;
    private LocalDateTime bookingTime;

    // Session info
    private Long sessionId;
    private String sessionName;
    private String sessionDescription;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String metaData;

    // Coach info
    private Long coachId;
    private String coachName;

    // Participant info
    private Long participantId;
    private String participantName;
}