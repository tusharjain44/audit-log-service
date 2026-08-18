package com.example.audit.controller;

import com.example.audit.model.AuditLog;
import com.example.audit.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping("/events")
    public ResponseEntity<AuditLog> writeEvent(@RequestBody AuditLog event) {
        return ResponseEntity.ok(auditLogService.createEvent(event));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AuditLog>> queryEvents(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Instant fromInstant = (from != null) ? Instant.parse(from) : null;
        Instant toInstant = (to != null) ? Instant.parse(to) : null;

        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<AuditLog> results = auditLogService.queryEvents(actorId, eventType, resourceType, resourceId, fromInstant, toInstant, pageable);

        return ResponseEntity.ok(results);
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        return ResponseEntity.ok(auditLogService.verifyChain());
    }
}