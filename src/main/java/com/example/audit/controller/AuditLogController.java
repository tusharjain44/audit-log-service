package com.example.audit.controller;

import com.example.audit.model.AuditLog;
import com.example.audit.model.AuditLogRequest;
import com.example.audit.service.AuditLogService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/audit")
public class AuditLogController {
    private static final Logger logger = LoggerFactory.getLogger(AuditLogController.class);
    private final AuditLogService auditLogService;
    
    // Idempotency cache to prevent replay attacks
    private final Set<String> processedRequests = ConcurrentHashMap.newKeySet();

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(
            @Valid @RequestBody AuditLogRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        if (idempotencyKey != null && !processedRequests.add(idempotencyKey)) {
            logger.warn("SECURITY_AUDIT: Replay attack or duplicate request blocked for key: {}", idempotencyKey);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Duplicate Request"));
        }

        AuditLog log = new AuditLog();
        log.setEventType(request.getEventType());
        log.setActorId(request.getActorId());
        log.setResourceType(request.getResourceType());
        log.setResourceId(request.getResourceId());
        log.setPayload(request.getPayload());
        return ResponseEntity.ok(auditLogService.createEvent(log));
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AuditLog>> queryEvents(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) String resourceId,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Authentication auth) {
        
        if (page < 0 || size <= 0 || size > 100) {
            throw new IllegalArgumentException("Pagination parameters out of bounds");
        }

        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            if (actorId != null && !actorId.equals(principal.getName())) {
                logger.warn("SECURITY_AUDIT: Cross-actor isolation violation attempt by {}", principal.getName());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            actorId = principal.getName();
        }

        return ResponseEntity.ok(auditLogService.queryEvents(actorId, eventType, resourceType, resourceId, from, to, PageRequest.of(page, size)));
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyChain() {
        return ResponseEntity.ok(auditLogService.verifyChain());
    }

    @PostMapping("/events/{id}/redact")
    public ResponseEntity<AuditLog> redactRecord(@PathVariable Long id) {
        logger.warn("ADMIN_AUDIT: Administrator initiated redaction on record ID: {}", id);
        return ResponseEntity.ok(auditLogService.redactRecord(id));
    }

    @DeleteMapping("/archive")
    public ResponseEntity<Integer> archiveOldRecords(@RequestParam Instant beforeDate) {
        logger.warn("ADMIN_AUDIT: Administrator initiated archival sweep before date: {}", beforeDate);
        return ResponseEntity.ok(auditLogService.archiveOldRecords(beforeDate));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportRecords(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) String resourceId) {
        return ResponseEntity.ok(auditLogService.exportRecords(actorId, resourceId));
    }
}