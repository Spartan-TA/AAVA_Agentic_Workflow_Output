package com.example.app.service.impl;

import com.example.app.dto.DashboardDto;
import com.example.app.entity.User;
import com.example.app.repository.UserRepository;
import com.example.app.service.DashboardService;
import com.example.app.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final UserRepository userRepository;

    @Override
    public DashboardDto getDashboardData(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        // Populate dashboard data
        return DashboardDto.fromUser(user);
    }

    @Override
    public DashboardDto getAdminDashboardData() {
        // Populate admin dashboard data
        return DashboardDto.adminDashboard();
    }
}
