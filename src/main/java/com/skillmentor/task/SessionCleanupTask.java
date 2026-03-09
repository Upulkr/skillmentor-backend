package com.skillmentor.task;

import com.skillmentor.entity.Session;
import com.skillmentor.entity.SessionStatus;
import com.skillmentor.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BACKGROUND TASK: Session Auto-Completion
 * runs every 5 minutes to check for finished sessions
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupTask {

    private final SessionRepository sessionRepository;

    /**
     * AUTO-COMPLETE SESSIONS
     * Logic: Find all CONFIRMED sessions where end time is in the past.
     * End Time = sessionDate + sessionTime + duration
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    @Transactional
    public void autoCompleteSessions() {
        log.info("Running background task: Session Auto-Completion check...");

        List<Session> activeSessions = sessionRepository.findByStatus(SessionStatus.CONFIRMED);

        LocalDateTime now = LocalDateTime.now();
        int completedCount = 0;

        for (Session session : activeSessions) {
            // Combine date and time to get a start timestamp
            LocalDateTime startTime = session.getSessionDate().atTime(session.getSessionTime());
            // Add duration to get end timestamp
            LocalDateTime endTime = startTime.plusMinutes(session.getDurationMinutes());

            // If end time is before 'now', mark as completed
            if (endTime.isBefore(now)) {
                session.setStatus(SessionStatus.COMPLETED);
                sessionRepository.save(session);
                completedCount++;
                log.info("Auto-completed Session ID: {} (Ended at: {})", session.getId(), endTime);
            }
        }

        if (completedCount > 0) {
            log.info("Background task finished. Auto-completed {} sessions.", completedCount);
        }
    }
}
