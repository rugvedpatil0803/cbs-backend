package com.project.cbsbackend.dto.session;

import com.project.cbsbackend.dto.adminspecial.ParticipantResponse;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionDetailResponse {
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

    private Integer maxSeat;
    private Integer occupiedSeats;
    private Integer availableSeats;

    private List<ParticipantResponse> participants;
}