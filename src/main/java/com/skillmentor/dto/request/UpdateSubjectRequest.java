package com.skillmentor.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubjectRequest {
    private String name;
    private String description;
    private String imageUrl;
    private Long mentorId;
}
