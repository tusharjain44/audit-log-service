package com.example.audit.controller;

import com.example.audit.model.AuditLog;
import com.example.audit.model.ExportBundle;
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

        return ResponseEntity.ok(auditLogService.queryEvents(actorId, eventType, resourceType, resourceId, fromInstant, toInstant, pageable));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        return ResponseEntity.ok(auditLogService.verifyChain());
    }

    @DeleteMapping("/archive")
    public ResponseEntity<String> archiveRecords(@RequestParam String beforeDate) {
        Instant threshold = Instant.parse(beforeDate);
        int archivedCount = auditLogService.archiveOldRecords(threshold);
        return ResponseEntity.ok("Archived " + archivedCount + " records older than " + beforeDate);
    }

    @PostMapping("/events/{id}/redact")
    public ResponseEntity<AuditLog> redactRecord(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.redactRecord(id));
    }

    @GetMapping("/export")
    public ResponseEntity<ExportBundle> exportRecords(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceId) {

        if (actorId == null && resourceId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(auditLogService.exportRecords(actorId, resourceId));
    }
}