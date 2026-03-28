package com.project.cbsbackend.dto.booking;

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

    private Long sessionId;
    private String sessionName;
    private String sessionDescription;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private String metaData;

    private Long coachId;
    private String coachName;

    private Long participantId;
    private String participantName;
}