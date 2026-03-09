package com.skillmentor.dto.request;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String role; // STUDENT, ADMIN, etc.
}
