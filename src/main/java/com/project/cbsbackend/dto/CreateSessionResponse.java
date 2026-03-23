package com.project.cbsbackend.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class CreateSessionResponse {
    private Long id;
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
}