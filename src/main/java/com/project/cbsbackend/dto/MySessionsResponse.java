package com.project.cbsbackend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MySessionsResponse {
    private List<SessionWithAvailabilityResponse> upcoming;
    private List<SessionWithAvailabilityResponse> ongoing;
    private List<SessionWithAvailabilityResponse> completed;
}