package com.skillmentor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSubjectRequest {

    @NotNull(message = "Mentor ID is required")
    private Long mentorId;

    @NotBlank(message = "Subject name is required")
    @Length(min = 3, max = 100, message = "Name must be 3-100 characters")
    private String name;

    private String description;

    private String imageUrl;
}
