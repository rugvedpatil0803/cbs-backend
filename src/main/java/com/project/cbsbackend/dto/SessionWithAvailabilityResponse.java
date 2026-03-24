package com.project.cbsbackend.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionWithAvailabilityResponse {
    private Long sessionId;
    private String name;
    private String description;
    private Long coachId;
    private String coachName;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer noOfSeats;
    private String metaData;

    // Availability fields
    private Integer maxSeat;
    private Integer occupiedSeats;
    private Integer availableSeats; // maxSeat - occupiedSeats
}