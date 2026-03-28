package com.project.cbsbackend.dto.adminspecial;

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

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePhoto;

    private String contactNumber;
    private String address;
    private String motivation;
    private String reason;
    private Integer preferredSessionDuration;
}