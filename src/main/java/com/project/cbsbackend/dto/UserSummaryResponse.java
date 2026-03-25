package com.project.cbsbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean isActive;
}