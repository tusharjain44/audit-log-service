package com.example.audit.controller;

import com.example.audit.dto.AuditLogRequest;
import com.example.audit.model.AuditLog;
import com.example.audit.model.ExportBundle;
import com.example.audit.service.AuditLogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/audit")
public class AuditLogController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuditLogController.class);

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping("/events")
    public ResponseEntity<AuditLog> writeEvent(@Valid @RequestBody AuditLogRequest request) {
        AuditLog log = new AuditLog();
        log.setEventType(request.getEventType());
        log.setActorId(request.getActorId());
        log.setResourceType(request.getResourceType());
        log.setResourceId(request.getResourceId());
        log.setPayload(request.getPayload());
        
        return ResponseEntity.ok(auditLogService.createEvent(log));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AuditLog>> getEvents(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            
        // Enforce strict pagination bounds
        int boundedSize = Math.min(size, 100);
        int boundedPage = Math.max(page, 0);

        return ResponseEntity.ok(auditLogService.queryEvents(
                actorId, eventType, resourceType, resourceId, from, to, PageRequest.of(boundedPage, boundedSize)));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        return ResponseEntity.ok(auditLogService.verifyChain());
    }

    @DeleteMapping("/archive")
    public ResponseEntity<String> archiveRecords(@RequestParam Instant beforeDate) {
        int count = auditLogService.archiveOldRecords(beforeDate);
        return ResponseEntity.ok("Archived " + count + " records older than " + beforeDate);
    }

    @PostMapping("/events/{id}/redact")
    public ResponseEntity<AuditLog> redactRecord(@PathVariable Long id) {
        logger.warn("ADMIN_AUDIT: Administrator initiated redaction on record ID: {}", id);
        return ResponseEntity.ok(auditLogService.redactRecord(id));
    }

    @GetMapping("/export")
    public ResponseEntity<ExportBundle> exportRecords(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceId) {
        return ResponseEntity.ok(auditLogService.exportRecords(actorId, resourceId));
    }
}