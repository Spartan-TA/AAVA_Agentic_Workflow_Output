package com.example.app.service;

import com.example.app.dto.DashboardDto;

public interface DashboardService {
    DashboardDto getDashboardData(String username);
    DashboardDto getAdminDashboardData();
}
