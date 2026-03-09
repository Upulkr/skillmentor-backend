package com.skillmentor.service;

import com.skillmentor.dto.response.DashboardStatsResponse;
import com.skillmentor.entity.PaymentStatus;
import com.skillmentor.repository.MentorRepository;
import com.skillmentor.repository.SessionRepository;
import com.skillmentor.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MentorRepository mentorRepository;
    private final SubjectRepository subjectRepository;
    private final SessionRepository sessionRepository;

    public DashboardStatsResponse getStats() {
        long totalMentors = mentorRepository.count();
        long totalSubjects = subjectRepository.count();

        // Active bookings = CONFIRMED or PENDING sessions that are not cancelled
        long activeBookings = sessionRepository.findAll().stream()
                .filter(s -> s.getStatus() != com.skillmentor.entity.SessionStatus.CANCELLED)
                .count();

        long pendingPayments = sessionRepository.countByPaymentStatus(PaymentStatus.PENDING);

        List<DashboardStatsResponse.RecentActivity> activities = new ArrayList<>();
        sessionRepository.findAll().stream()
                .sorted((a, b) -> {
                    java.time.LocalDateTime first = a.getCreatedAt() != null ? a.getCreatedAt()
                            : java.time.LocalDateTime.MIN;
                    java.time.LocalDateTime second = b.getCreatedAt() != null ? b.getCreatedAt()
                            : java.time.LocalDateTime.MIN;
                    return second.compareTo(first);
                })
                .limit(5)
                .forEach(session -> {
                    String timeAgo = calculateTimeAgo(session.getCreatedAt());
                    activities.add(DashboardStatsResponse.RecentActivity.builder()
                            .id(session.getId().toString())
                            .type("BOOKING")
                            .message("Booking Received: " + session.getSubject().getName() + " (Mentor: "
                                    + session.getMentor().getFirstName() + ")")
                            .timeAgo(timeAgo)
                            .build());
                });

        return DashboardStatsResponse.builder()
                .totalMentors(totalMentors)
                .totalSubjects(totalSubjects)
                .totalBookings(activeBookings)
                .pendingPayments(pendingPayments)
                .recentActivities(activities)
                .build();
    }

    private String calculateTimeAgo(java.time.LocalDateTime dateTime) {
        if (dateTime == null)
            return "Unknown";
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60)
            return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60)
            return minutes + " mins ago";
        long hours = minutes / 60;
        if (hours < 24)
            return hours + " hours ago";
        long days = hours / 24;
        return days + " days ago";
    }
}
