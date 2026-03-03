/**
 * REQUEST DTO for submitting a review
 */
package com.skillmentor.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitReviewRequest {

    /**
     * Which session is this review for?
     */
    @NotNull(message = "Session ID is required")
    private Long sessionId;

    /**
     * Star rating - 1 to 5
     *
     * WHY @Min(1) @Max(5)?
     * - Can only rate 1-5 stars
     * - 0 stars or 6 stars = invalid
     * - Prevents garbage data
     */
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer rating;

    /**
     * Review text - What did you think?
     *
     * WHY optional?
     * - Could just rate without comment
     * - Or could require both with @NotBlank
     * - Depends on business rules
     */
    @Length(max = 1000, message = "Review cannot exceed 1000 characters")
    private String reviewText;
}