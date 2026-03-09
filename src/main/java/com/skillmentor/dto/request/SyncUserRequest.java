package com.skillmentor.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncUserRequest {
    @NotBlank(message = "Clerk ID is required")
    private String clerkId;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    private String firstName;
    private String lastName;
    private String profileImageUrl;
    private String role; // From Clerk metadata
}
