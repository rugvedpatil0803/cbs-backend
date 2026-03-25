package com.project.cbsbackend.dto.feedback;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackResponse {
    private Long feedbackId;

    // Session info
    private Long sessionId;
    private String sessionName;

    // User info
    private Long userId;
    private String userName;

    private Integer rating;
    private String feedbackDesc;
    private LocalDateTime createdAt;
}