package com.skillmentor.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalMentors;
    private long totalSubjects;
    private long totalBookings;
    private long pendingPayments;
    private List<RecentActivity> recentActivities;

    @Data
    @Builder
    public static class RecentActivity {
        private String id;
        private String type;
        private String message;
        private String timeAgo;
    }
}
