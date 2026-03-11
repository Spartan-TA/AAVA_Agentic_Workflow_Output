package com.example.app.dto;

import com.example.app.entity.User;
import lombok.Data;

@Data
public class DashboardDto {
    private String username;
    private String email;
    private int notificationCount;
    private int activityCount;
    private boolean isAdmin;

    public static DashboardDto fromUser(User user) {
        DashboardDto dto = new DashboardDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setNotificationCount(user.getNotifications() != null ? user.getNotifications().size() : 0);
        dto.setActivityCount(user.getActivities() != null ? user.getActivities().size() : 0);
        dto.setIsAdmin(false); // Set based on role if available
        return dto;
    }

    public static DashboardDto adminDashboard() {
        DashboardDto dto = new DashboardDto();
        dto.setIsAdmin(true);
        // Populate admin dashboard data
        return dto;
    }
}
