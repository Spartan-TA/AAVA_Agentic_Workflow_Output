package com.example.app.controller;

import com.example.app.dto.DashboardDto;
import com.example.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal Principal principal) {
        DashboardDto dashboard = dashboardService.getDashboardData(principal.getName());
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/admin")
    public ResponseEntity<DashboardDto> getAdminDashboard() {
        DashboardDto dashboard = dashboardService.getAdminDashboardData();
        return ResponseEntity.ok(dashboard);
    }
}
