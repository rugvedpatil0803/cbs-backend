package com.project.cbsbackend.dto.adminspecial;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookingListResponse {
    private Long bookingId;
    private String bookingTime;

    private Long sessionId;
    private String sessionName;
    private String sessionDescription;
    private String startDay;
    private String endDay;
    private String startTime;
    private String endTime;
    private String metaData;

    private Long coachId;
    private String coachName;

    private Boolean isActive;
    private Boolean isDeleted;
}