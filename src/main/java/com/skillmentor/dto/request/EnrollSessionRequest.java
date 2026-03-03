/**
 * REQUEST DTO for creating a session
 *
 * WHY separate from entity?
 * - Entity has @Id, @GeneratedValue (auto-set by DB)
 * - Request DTO doesn't have those (client can't set them)
 * - Entity has timestamps (set by @CreationTimestamp)
 * - Request DTO doesn't need them
 *
 * This ensures clients can only set what we allow!
 */
package com.skillmentor.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollSessionRequest {

    /**
     * Mentor ID - Which mentor to book?
     *
     * WHY @NotNull?
     * - Session must have a mentor
     * - If null, should reject with 400 Bad Request
     *
     * Spring validates this automatically before method is called
     */
    @NotNull(message = "Mentor ID is required")
    private Long mentorId;

    /**
     * Subject ID - Which subject?
     *
     * WHY @NotNull?
     * - Session must have a subject
     */
    @NotNull(message = "Subject ID is required")
    private Long subjectId;

    /**
     * Session Date - When?
     *
     * WHY @NotNull?
     * - Date is required
     *
     * WHY @FutureOrPresent?
     * - Can't book sessions in the past!
     * - Automatic validation prevents it
     * - Returns 400 if date is past
     */
    @NotNull(message = "Session date is required")
    @FutureOrPresent(message = "Session date must be in the future")
    private LocalDate sessionDate;

    /**
     * Session Time - What time?
     *
     * WHY @NotNull?
     * - Time is required
     */
    @NotNull(message = "Session time is required")
    private LocalTime sessionTime;

    /**
     * Duration in minutes
     *
     * WHY @Min(15) @Max(480)?
     * - Min: 15 minutes (meaningful session)
     * - Max: 480 minutes = 8 hours (reasonable limit)
     * - Prevents: 1-minute sessions or 24-hour sessions
     */
    @NotNull(message = "Duration is required")
    @Min(value = 15, message = "Session must be at least 15 minutes")
    @Max(value = 480, message = "Session cannot exceed 8 hours")
    private Integer durationMinutes;

    /**
     * VALIDATION IN ACTION
     *
     * When this DTO is used with @Valid annotation:
     *
     * POST /api/v1/sessions/enroll
     * {
     *   "mentorId": null,     // ← @NotNull fails → 400 Bad Request
     *   "subjectId": 1,
     *   ...
     * }
     *
     * Spring returns:
     * {
     *   "message": "Validation failed",
     *   "errors": {
     *     "mentorId": "Mentor ID is required"
     *   }
     * }
     */
}