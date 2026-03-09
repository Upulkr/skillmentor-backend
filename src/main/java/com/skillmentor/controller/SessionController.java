package com.skillmentor.controller;

import com.skillmentor.dto.request.EnrollSessionRequest;
import com.skillmentor.dto.response.SessionResponse;
import com.skillmentor.entity.User;
import com.skillmentor.entity.UserRole;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.UserRepository;
import com.skillmentor.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SESSION CONTROLLER
 */
@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
@Slf4j
public class SessionController {

    private final SessionService sessionService;
    private final UserRepository userRepository;

    /**
     * BOOK A SESSION
     * 
     * @AuthenticationPrincipal String clerkId
     *                          Spring Security injects the 'userId' we set in
     *                          JwtAuthenticationFilter.
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public ResponseEntity<SessionResponse> enrollSession(
            @RequestBody @Valid EnrollSessionRequest request,
            @AuthenticationPrincipal String clerkId) {
        // Fetch local database ID using Clerk ID
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with clerkId: " + clerkId));

        SessionResponse response = sessionService.enrollSession(user.getId(), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET MY SESSIONS (Student Dashboard)
     */
    @GetMapping("/my-sessions")
    public List<SessionResponse> getMySessions(@AuthenticationPrincipal String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return sessionService.getStudentSessions(user.getId());
    }

    /**
     * GET SINGLE SESSION
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'MENTOR', 'ADMIN')")
    public SessionResponse getSession(@PathVariable Long id, @AuthenticationPrincipal String clerkId) {
        User user = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SessionResponse response = sessionService.getSessionById(id);

        // Security check: Students can only see their own sessions
        if (user.getRole().equals(UserRole.STUDENT) && !response.getStudentId().equals(user.getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view this session");
        }

        return response;
    }

    /**
     * ADMIN: Manage Bookings
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SessionResponse> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @PostMapping("/admin/{id}/confirm-payment")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponse confirmPayment(@PathVariable Long id) {
        return sessionService.confirmPayment(id);
    }

    @PostMapping("/admin/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponse markComplete(@PathVariable Long id) {
        return sessionService.markSessionComplete(id);
    }

    @PostMapping("/admin/{id}/meeting-link")
    @PreAuthorize("hasRole('ADMIN')")
    public SessionResponse addMeetingLink(
            @PathVariable Long id,
            @RequestBody com.skillmentor.dto.request.MeetingLinkRequest request) {
        log.info("Adding meeting link for session: {} - URL: {}", id, request.getMeetingLink());
        return sessionService.addMeetingLink(id, request.getMeetingLink());
    }

    /**
     * UPLOAD PAYMENT SLIP
     */
    @PostMapping("/{id}/payment-slip")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<SessionResponse> uploadPaymentSlip(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        System.out.println(">>> HIT ENDPOINT: POST /api/v1/sessions/" + id + "/payment-slip");
        org.slf4j.LoggerFactory.getLogger(SessionController.class).info("Uploading payment slip for session ID: {}",
                id);

        SessionResponse response = sessionService.uploadPaymentSlip(id, file);
        return ResponseEntity.ok(response);
    }
}
