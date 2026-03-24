package com.project.cbsbackend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFeedbackRequest {
    private Long sessionId;
    private Integer rating;
    private String feedbackDesc;
}