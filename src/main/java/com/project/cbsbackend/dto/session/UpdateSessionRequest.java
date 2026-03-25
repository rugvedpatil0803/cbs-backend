package com.project.cbsbackend.dto.session;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class UpdateSessionRequest {
    private String name;
    private String description;
    private LocalDate startDay;
    private LocalDate endDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer noOfSeats;
    private String metaData;
}