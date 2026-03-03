/**
 * RESPONSE DTO for Mentor Profile
 *
 * What to show when user views mentor profile
 */
package com.skillmentor.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorResponse {

    /**
     * Mentor ID
     */
    private Long id;

    /**
     * Personal Information
     */
    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    /**
     * Professional Information
     */
    private String title;
    private String profession;
    private String company;
    private Integer experienceYears;
    private String bio;
    private String profileImageUrl;
    private Boolean isCertified;
    private Integer startYear;

    /**
     * Statistics
     *
     * WHY add these?
     * - Show on mentor profile
     * - "42 students taught"
     * - "4.8 stars from 120 reviews"
     * - These should be calculated, not stored
     *
     * Could be calculated in service:
     * double avgRating = reviewService.getAverageRating(mentorId);
     * int studentCount = sessionService.countUniqueStudents(mentorId);
     */
    private Double averageRating;
    private Integer reviewCount;
    private Integer totalStudents;

    /**
     * Subjects taught
     *
     * WHY List<SubjectResponse>?
     * - Show on mentor profile
     * - Each subject card shows: image, name, description, enrollment count
     */
    private List<SubjectResponse> subjects;

    /**
     * Timestamps
     */
    private LocalDateTime createdAt;
}