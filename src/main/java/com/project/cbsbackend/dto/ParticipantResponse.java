package com.project.cbsbackend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantResponse {
    private Long bookingId;
    private LocalDateTime bookingTime;

    // From tbl_user
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePhoto;

    // From tbl_user_info (nullable)
    private String contactNumber;
    private String address;
    private String motivation;
    private String reason;
    private Integer preferredSessionDuration;
}