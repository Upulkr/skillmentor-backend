package com.skillmentor.repository;

import com.skillmentor.entity.Session;
import com.skillmentor.entity.SessionStatus;
import com.skillmentor.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * SESSION REPOSITORY
 *
 * Most critical repository!
 * Here's where we prevent double-booking and implement business logic.
 */
@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

        /**
         * Find all sessions for a student
         *
         * Student dashboard: "Your upcoming sessions"
         *
         * WHY?
         * - Student logs in
         * - We need to show their sessions
         * - Query: SELECT * FROM sessions WHERE student_id = ?
         */
        List<Session> findByStudentId(Long studentId);

        /**
         * Find all sessions for a mentor
         *
         * Mentor dashboard: "Your scheduled sessions"
         */
        List<Session> findByMentorId(Long mentorId);

        /**
         * Find all sessions for a specific subject
         */
        List<Session> findBySubjectId(Long subjectId);

        /**
         * Find sessions for a mentor on a specific date
         */
        List<Session> findByMentorIdAndSessionDate(Long mentorId, LocalDate sessionDate);

        /**
         * Find sessions by status
         *
         * Admin dashboard: Filter "Show only pending sessions"
         */
        List<Session> findByStatus(SessionStatus status);

        /**
         * Find sessions by payment status
         *
         * Admin dashboard: "Approve pending payments"
         */
        List<Session> findByPaymentStatus(PaymentStatus paymentStatus);

        /**
         * Find sessions needing payment confirmation
         *
         * Admin sees: "5 sessions waiting for payment approval"
         */
        List<Session> findByStatusAndPaymentStatus(
                        SessionStatus status,
                        PaymentStatus paymentStatus);

        /**
         * PAGINATION & FILTERING for admin
         *
         * This would need Spring Data's Pageable interface
         * (not shown here, but important for large datasets)
         *
         * Usage:
         * Page<Session> page = sessionRepository.findAll(
         * PageRequest.of(0, 20) // Page 0, 20 items per page
         * );
         */

        /**
         * Find a specific session and ensure it belongs to the student
         *
         * Security: Student can only see/modify their own sessions
         *
         * Usage:
         * Optional<Session> session = sessionRepository
         * .findByIdAndStudentId(sessionId, studentId);
         * // Returns empty if session doesn't belong to this student
         */
        Optional<Session> findByIdAndStudentId(Long sessionId, Long studentId);

        /**
         * Count pending sessions
         *
         * "You have 3 sessions waiting for payment approval"
         */
        long countByStatus(SessionStatus status);

        /**
         * Count pending payments for admin
         *
         * "5 payments waiting for approval"
         */
        long countByPaymentStatus(PaymentStatus paymentStatus);
}