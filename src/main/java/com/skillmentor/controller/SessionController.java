package com.skillmentor.controller;

import com.skillmentor.dto.request.EnrollSessionRequest;
import com.skillmentor.dto.response.MentorResponse;
import com.skillmentor.dto.response.SessionResponse;
import com.skillmentor.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SESSION CONTROLLER
 *
 * Showing how to use DTOs properly
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    /**
     * BOOK A SESSION
     *
     * Request: EnrollSessionRequest (DTO)
     * Response: SessionResponse (DTO)
     *
     * WHY not use Session entity directly?
     * - Entity has @Id, @GeneratedValue (can't set from API)
     * - Entity has relationships (causes lazy-loading issues)
     * - Entity has sensitive fields
     * - Entity changes break API
     * - DTO is the API contract - should be stable
     */
    @PostMapping("/enroll")
    public ResponseEntity<SessionResponse> enrollSession(
            /**
             * @Valid tells Spring:
             * - Validate the DTO using @NotNull, @Email, etc.
             * - If validation fails, return 400 Bad Request
             * - Don't call controller method
             */
            @RequestBody @Valid EnrollSessionRequest request,
            @RequestHeader("Authorization") String bearerToken
    ) {
        // Extract user from JWT (more on this later)
        Long studentId = extractUserIdFromToken(bearerToken);

        // Call service
        SessionResponse response = sessionService.enrollSession(studentId, request);

        // Return with 201 Created status
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * GET ALL SESSIONS
     *
     * Response: List of SessionResponse (DTOs)
     */
    @GetMapping("/sessions")

    public List<SessionResponse> getAllSessions() {
        return sessionService.getAllSessions();
        // Returns: [{ id: 1, mentorName: "John", ... }, ...]
    }

    /**
     * GET MENTOR PROFILE
     *
     * Response: MentorResponse (DTO with full profile)
     */
    @GetMapping("/mentors/{id}")
    public MentorResponse getMentorProfile(@PathVariable Long id) {
        return mentorService.getMentorProfile(id);
        // Returns full profile with:
        // - Mentor info
        // - List of subjects taught
        // - Statistics (avg rating, student count)
        // - Calculated values (not just stored data)
    }
}
