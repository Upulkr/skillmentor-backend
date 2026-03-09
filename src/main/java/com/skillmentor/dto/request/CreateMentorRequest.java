/**
 * REQUEST DTO for creating a mentor
 *
 * Admin uses this to create new mentors
 */
package com.skillmentor.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateMentorRequest {

    /**
     * Basic Information
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be 2-50 characters")
    private String lastName;

    /**
     * Email - Must be valid format
     *
     * WHY @Email?
     * - Validates email format (must have @, domain, etc.)
     * - Returns 400 if invalid
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    /**
     * Phone
     */
    @Pattern(regexp = "^[+]?[0-9]{10,13}$", message = "Phone number must be valid")
    private String phone;

    /**
     * Professional Information
     */
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be 3-100 characters")
    private String title;

    @NotBlank(message = "Profession is required")
    private String profession;

    @NotBlank(message = "Company is required")
    private String company;

    /**
     * Experience - Years
     *
     * WHY @Min(0) @Max(70)?
     * - Can't have negative experience
     * - Max 70 is reasonable (career can't be >70 years)
     */
    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience must be at least 0")
    @Max(value = 70, message = "Experience cannot exceed 70 years")
    private Integer experienceYears;

    /**
     * Bio - Professional biography
     */
    @Size(max = 2000, message = "Bio cannot exceed 2000 characters")
    private String bio;

    /**
     * Profile Image URL
     */
    private String profileImageUrl;

    /**
     * Certification status
     */
    @Builder.Default
    private Boolean isCertified = false;

    /**
     * Start Year - When did they start teaching?
     */
    private Integer startYear;
}