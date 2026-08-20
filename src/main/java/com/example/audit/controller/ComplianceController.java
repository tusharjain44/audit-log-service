package com.example.audit.controller;

import com.example.audit.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/audit/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/report")
    public ResponseEntity<Map<String, Long>> getReport(
            @RequestParam Instant start,
            @RequestParam Instant end) {
        return ResponseEntity.ok(complianceService.generateReport(start, end));
    }
}