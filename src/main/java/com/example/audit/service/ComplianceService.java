package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.model.ComplianceSummary;
import com.example.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComplianceService {

    private final AuditLogRepository repository;

    public ComplianceService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public ComplianceSummary generateReport(Instant from, Instant to) {
        List<AuditLog> events = repository.findByTimestampBetweenAndIsArchivedFalse(from, to);

        Map<String, Long> eventsByType = events.stream()
                .collect(Collectors.groupingBy(AuditLog::getEventType, Collectors.counting()));

        return new ComplianceSummary(from, to, events.size(), eventsByType);
    }

    public void analyzeForAnomalies(AuditLog event) {
        // Heuristic: Flag if an actor performs more than 5 actions in the last 1 minute
        Instant oneMinuteAgo = Instant.now().minus(1, ChronoUnit.MINUTES);
        long recentActivityCount = repository.countByActorIdAndTimestampAfter(event.getActorId(), oneMinuteAgo);

        if (recentActivityCount > 5) {
            // In a production environment, this would push to an alert queue (e.g., Kafka or AWS SNS).
            // For this assessment, logging it clearly to standard error is sufficient.
            System.err.println("🚨 COMPLIANCE ALERT: High frequency activity detected! Actor '"
                    + event.getActorId() + "' has generated " + recentActivityCount + " events in the last minute.");
        }
    }
}