package com.skillmentor.service;

import com.skillmentor.dto.request.EnrollSessionRequest;
import com.skillmentor.dto.response.SessionResponse;
import com.skillmentor.entity.*;
import com.skillmentor.exception.DoubleBookingException;
import com.skillmentor.exception.ResourceNotFoundException;
import com.skillmentor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

/**
 * SESSION SERVICE
 *
 * WHY @Service?
 * - Tells Spring: "This is a business logic component"
 * - Spring creates a bean for this class
 * - Enables @Transactional support
 *
 * WHY @RequiredArgsConstructor?
 * - Lombok annotation
 * - Auto-generates constructor with all final fields
 * - Injects all repositories automatically
 * - Much cleaner than writing constructor manually!
 *
 * COMPARISON:
 *
 * WITHOUT Lombok:
 * public class SessionService {
 *     private final SessionRepository sessionRepository;
 *     private final MentorRepository mentorRepository;
 *     private final SubjectRepository subjectRepository;
 *
 *     public SessionService(
 *         SessionRepository sr,
 *         MentorRepository mr,
 *         SubjectRepository subr
 *     ) {
 *         this.sessionRepository = sr;
 *         this.mentorRepository = mr;
 *         this.subjectRepository = subr;
 *     }
 * }
 *
 * WITH Lombok (@RequiredArgsConstructor):
 * @RequiredArgsConstructor
 * public class SessionService {
 *     private final SessionRepository sessionRepository;
 *     private final MentorRepository mentorRepository;
 *     private final SubjectRepository subjectRepository;
 *     // Constructor auto-generated!
 * }
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    /**
     * ENROLL SESSION (Most critical method!)
     *
     * Student books a session with a mentor.
     *
     * STEPS:
     * 1. Validate request
     * 2. Check: Is date in future? (not in past)
     * 3. Check: Does mentor exist?
     * 4. Check: Does subject exist?
     * 5. Check: Is there double-booking? (mentor already booked at that time)
     * 6. Create session
     * 7. Return session details
     *
     * WHY @Transactional(isolation = Isolation.SERIALIZABLE)?
     *
     * TRANSACTION = All-or-nothing database operation
     *
     * Example without transaction:
     * 1. Check: No overlapping sessions ✓
     * 2. CONTEXT SWITCH: Another request comes in
     * 3. That request: Check: No overlapping sessions ✓
     * 4. Both requests: Create session ❌ (DOUBLE-BOOKING!)
     *
     * With @Transactional(isolation = SERIALIZABLE):
     * 1. Request A: START TRANSACTION
     * 2. Request A: Check overlapping sessions
     * 3. Request A: LOCKED (no other requests interfere)
     * 4. Request A: Create session
     * 5. Request A: COMMIT
     * 6. Request B: Now can proceed
     *
     * SERIALIZABLE = Highest isolation level
     * - Other transactions can't interfere
     * - Prevents double-booking
     * - Trade-off: Slower, but correct data
     *
     * For this critical operation, correctness > performance!
     */
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.SERIALIZABLE)
    public SessionResponse enrollSession(Long studentId, EnrollSessionRequest request) {

        // STEP 1: Validate input
        if (request.getMentorId() == null || request.getSubjectId() == null) {
            throw new IllegalArgumentException("Mentor and subject are required");
        }

        // STEP 2: Validate date is in future
        if (request.getSessionDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Session date must be in the future");
        }

        // STEP 3: Fetch mentor (throw exception if not found)
        Mentor mentor = mentorRepository.findById(request.getMentorId())
                .orElseThrow(() -> new ResourceNotFoundException("Mentor not found"));

        // STEP 4: Fetch subject (throw exception if not found)
        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        // STEP 5: Verify subject belongs to this mentor
        if (!subject.getMentor().getId().equals(mentor.getId())) {
            throw new IllegalArgumentException("Subject does not belong to this mentor");
        }

        // STEP 6: CRITICAL - Check for overlapping sessions
        // Calculate end time: startTime + duration
        LocalTime endTime = request.getSessionTime()
                .plusMinutes(request.getDurationMinutes());

        List<Session> overlappingSessions = sessionRepository.findOverlappingSessions(
                mentor.getId(),
                request.getSessionDate(),
                request.getSessionTime(),
                endTime
        );

        if (!overlappingSessions.isEmpty()) {
            // Double-booking detected!
            throw new DoubleBookingException(
                    "Mentor is not available at this time. Overlapping session exists."
            );
        }

        // STEP 7: Create session
        Session session = Session.builder()
                .student(userRepository.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found")))
                .mentor(mentor)
                .subject(subject)
                .sessionDate(request.getSessionDate())
                .sessionTime(request.getSessionTime())
                .durationMinutes(request.getDurationMinutes())
                .status(SessionStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        // Save to database (within transaction)
        Session savedSession = sessionRepository.save(session);

        // STEP 8: Return response
        return convertToResponse(savedSession);
    }

    /**
     * GET STUDENT'S SESSIONS
     *
     * Student dashboard: Show all my sessions
     */
    public List<SessionResponse> getStudentSessions(Long studentId) {
        List<Session> sessions = sessionRepository.findByStudentId(studentId);
        return sessions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * GET ALL SESSIONS (Admin only)
     *
     * Admin dashboard: Show all sessions for management
     */
    public List<SessionResponse> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        return sessions.stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * CONFIRM PAYMENT
     *
     * Admin approves payment. Change status: PENDING → CONFIRMED
     *
     * WHY @Transactional?
     * - Ensures status update happens or doesn't (no partial updates)
     * - Automatic rollback on exception
     */
    @Transactional
    public SessionResponse confirmPayment(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Validate: Can only confirm PENDING payments
        if (session.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Can only confirm pending payments"
            );
        }

        // Update payment status
        session.setPaymentStatus(PaymentStatus.CONFIRMED);
        session.setStatus(SessionStatus.CONFIRMED);

        // Save (within transaction)
        Session updated = sessionRepository.save(session);

        return convertToResponse(updated);
    }

    /**
     * MARK SESSION COMPLETE
     *
     * After session is delivered, admin marks it complete.
     * Change status: CONFIRMED → COMPLETED
     * Now student can write review.
     */
    @Transactional
    public SessionResponse markSessionComplete(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        // Validate: Can only complete CONFIRMED sessions
        if (session.getStatus() != SessionStatus.CONFIRMED) {
            throw new IllegalArgumentException(
                    "Can only complete confirmed sessions"
            );
        }

        session.setStatus(SessionStatus.COMPLETED);

        Session updated = sessionRepository.save(session);

        return convertToResponse(updated);
    }

    /**
     * ADD MEETING LINK
     *
     * Admin adds Zoom/Google Meet link before session
     */
    @Transactional
    public SessionResponse addMeetingLink(Long sessionId, String meetingLink) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));

        session.setMeetingLink(meetingLink);

        Session updated = sessionRepository.save(session);

        return convertToResponse(updated);
    }

    /**
     * HELPER METHOD: Convert Entity to DTO
     *
     * WHY DTOs?
     *
     * DTO = Data Transfer Object
     * Used to transfer data between layers
     *
     * Bad (returning entity):
     * @GetMapping("/sessions/{id}")
     * public Session getSession(Long id) {
     *     return sessionRepository.findById(id); // Returns full entity
     * }
     *
     * Problems:
     * - Expose internal structure
     * - Can't customize response format
     * - Lazy-loading issues (Hibernate)
     *
     * Good (returning DTO):
     * @GetMapping("/sessions/{id}")
     * public SessionResponse getSession(Long id) {
     *     Session session = sessionRepository.findById(id);
     *     return convertToResponse(session); // Return controlled DTO
     * }
     *
     * Benefits:
     * - Clean API contract
     * - Hide internal structure
     * - Control exactly what's sent
     * - Easier to evolve (change DTO without changing entity)
     */
    private SessionResponse convertToResponse(Session session) {
        return SessionResponse.builder()
                .id(session.getId())
                .mentorId(session.getMentor().getId())
                .mentorName(session.getMentor().getFirstName() + " " +
                        session.getMentor().getLastName())
                .subjectId(session.getSubject().getId())
                .subjectName(session.getSubject().getName())
                .sessionDate(session.getSessionDate())
                .sessionTime(session.getSessionTime())
                .durationMinutes(session.getDurationMinutes())
                .status(session.getStatus().toString())
                .paymentStatus(session.getPaymentStatus().toString())
                .meetingLink(session.getMeetingLink())
                .createdAt(session.getCreatedAt())
                .build();
    }
}