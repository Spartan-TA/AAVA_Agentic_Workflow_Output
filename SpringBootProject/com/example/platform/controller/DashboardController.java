package com.example.platform.controller;

import com.example.platform.dto.DashboardDto;
import com.example.platform.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard(@AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(dashboardService.getDashboard(userId));
    }
}
