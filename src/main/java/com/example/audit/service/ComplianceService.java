package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ComplianceService {
    private static final Logger logger = LoggerFactory.getLogger(ComplianceService.class);
    private final AuditLogRepository repository;

    public ComplianceService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void analyzeForAnomalies(AuditLog event) {
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        int recentEvents = repository.countByActorIdAndTimestampAfter(event.getActorId(), oneMinuteAgo);
        
        if (recentEvents > 5) {
            logger.warn("SECURITY_AUDIT_ALERT: High frequency activity detected! Actor [{}] generated {} events in the last minute.", 
                event.getActorId(), recentEvents);
        }
    }

    public Map<String, Long> generateReport(Instant start, Instant end) {
        // FIXED ARCH-06: Query database directly instead of loading entities into memory
        List<Object[]> results = repository.countEventsByType(start, end);
        Map<String, Long> report = new HashMap<>();
        for (Object[] row : results) {
            report.put((String) row[0], (Long) row[1]);
        }
        return report;
    }
}