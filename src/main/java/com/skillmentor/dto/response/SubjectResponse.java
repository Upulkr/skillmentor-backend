package com.skillmentor.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectResponse {
    private Long id;
    private Long mentorId;
    private String mentorName;
    private String name;
    private String description;
    private String imageUrl;
    private Integer enrollmentCount;
    private LocalDateTime createdAt;
}
