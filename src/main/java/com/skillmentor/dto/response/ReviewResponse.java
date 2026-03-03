package com.skillmentor.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    /**
     * Review ID
     */
    private Long id;

    /**
     * Session ID (which session?)
     */
    private Long sessionId;

    /**
     * Student information
     *
     * WHY not full StudentResponse?
     * - Just show name, not email or sensitive data
     */
    private Long studentId;
    private String studentName;

    /**
     * Mentor information
     */
    private Long mentorId;
    private String mentorName;

    /**
     * Review content
     */
    private Integer rating;
    private String reviewText;

    /**
     * When was it posted?
     */
    private LocalDateTime createdAt;
}