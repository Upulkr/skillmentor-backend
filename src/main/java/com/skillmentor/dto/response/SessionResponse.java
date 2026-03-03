/**
 * RESPONSE DTO for Session
 *
 * WHY not return Session entity?
 * - Entity has relationships (mentor, student, subject objects)
 * - Causes N+1 query problem (loads too much data)
 * - Exposes internal structure
 * - Can change without breaking client
 *
 * This DTO only includes what frontend NEEDS
 */
package com.skillmentor.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionResponse {

    /**
     * Session ID - Uniquely identifies this session
     */
    private Long id;

    /**
     * Mentor information
     *
     * WHY separate fields instead of MentorResponse?
     * - Frontend just needs name + ID
     * - Keeps response small
     * - Avoid nested objects unless needed
     *
     * Example response would be smaller:
     * { "mentorId": 1, "mentorName": "John Doe", ... }
     *
     * vs
     *
     * { "mentor": { "id": 1, "name": "John", ..., extra fields }, ... }
     */
    private Long mentorId;
    private String mentorName;

    /**
     * Subject information
     */
    private Long subjectId;
    private String subjectName;

    /**
     * Session details
     */
    private LocalDate sessionDate;
    private LocalTime sessionTime;
    private Integer durationMinutes;

    /**
     * Status - What's the state?
     *
     * WHY String instead of Enum?
     * - JSON doesn't have enums
     * - Could be "PENDING", "CONFIRMED", "COMPLETED"
     * - Frontend treats as string
     *
     * Alternative: Could return as object
     * "status": { "name": "PENDING", "displayName": "Waiting for payment" }
     */
    private String status;

    /**
     * Payment Status
     */
    private String paymentStatus;

    /**
     * Meeting Link - Zoom/Google Meet URL
     *
     * WHY optional?
     * - Admin adds it before session
     * - Null until admin adds it
     * - Frontend can conditionally show it
     */
    private String meetingLink;

    /**
     * When was this session created?
     *
     * WHY return this?
     * - Frontend can show "booked 2 days ago"
     * - Audit trail
     */
    private LocalDateTime createdAt;
}