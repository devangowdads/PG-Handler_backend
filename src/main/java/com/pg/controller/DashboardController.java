package com.pg.controller;

import com.pg.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) { this.dashboardService = dashboardService; }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() { return ResponseEntity.ok(dashboardService.getStats()); }
}
