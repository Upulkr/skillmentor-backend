package com.skillmentor.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String clerkId;
    private String email;
    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String role;
}
