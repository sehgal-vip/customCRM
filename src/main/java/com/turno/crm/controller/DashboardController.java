package com.turno.crm.controller;

import com.turno.crm.model.dto.*;
import com.turno.crm.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@PreAuthorize("hasRole('MANAGER')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/pipeline-overview")
    public ResponseEntity<PipelineOverviewResponse> getPipelineOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) List<Long> agentIds,
            @RequestParam(required = false) List<Long> regionIds) {
        return ResponseEntity.ok(dashboardService.getPipelineOverview(dateFrom, dateTo, agentIds, regionIds));
    }

    @GetMapping("/agent-performance")
    public ResponseEntity<AgentPerformanceResponse> getAgentPerformance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ResponseEntity.ok(dashboardService.getAgentPerformance(dateFrom, dateTo));
    }

    @GetMapping("/win-loss")
    public ResponseEntity<WinLossResponse> getWinLoss(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo) {
        return ResponseEntity.ok(dashboardService.getWinLoss(dateFrom, dateTo));
    }

    @GetMapping("/objection-heatmap")
    public ResponseEntity<ObjectionHeatmapResponse> getObjectionHeatmap(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime dateTo,
            @RequestParam(required = false) Long agentId) {
        return ResponseEntity.ok(dashboardService.getObjectionHeatmap(dateFrom, dateTo, agentId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<DashboardAlertsResponse> getAlerts() {
        return ResponseEntity.ok(dashboardService.getAlerts());
    }
}
