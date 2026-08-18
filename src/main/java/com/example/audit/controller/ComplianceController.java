package com.example.audit.controller;

import com.example.audit.model.ComplianceSummary;
import com.example.audit.service.ComplianceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/audit/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @GetMapping("/report")
    public ResponseEntity<ComplianceSummary> getReport(
            @RequestParam String from,
            @RequestParam String to) {

        Instant fromInstant = Instant.parse(from);
        Instant toInstant = Instant.parse(to);

        return ResponseEntity.ok(complianceService.generateReport(fromInstant, toInstant));
    }
}