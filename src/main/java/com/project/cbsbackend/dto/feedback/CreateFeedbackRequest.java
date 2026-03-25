package com.project.cbsbackend.dto.feedback;

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