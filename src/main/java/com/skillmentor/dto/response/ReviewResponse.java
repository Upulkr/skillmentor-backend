package com.skillmentor.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private String studentName;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
}