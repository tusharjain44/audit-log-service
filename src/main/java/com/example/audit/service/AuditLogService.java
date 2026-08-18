package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.model.ExportBundle;
import com.example.audit.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuditLogService {

    private final AuditLogRepository repository;
    private final ComplianceService complianceService;
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    private static final String REDACTED_PAYLOAD_FLAG = "[REDACTED_FOR_PRIVACY]";

    public AuditLogService(AuditLogRepository repository, @Lazy ComplianceService complianceService) {
        this.repository = repository;
        this.complianceService = complianceService;
    }

    @Transactional
    public AuditLog createEvent(AuditLog event) {
        event.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        Optional<AuditLog> lastRecord = repository.findTopByOrderByIdDesc();
        String prevHash = lastRecord.map(AuditLog::getCurrentHash).orElse(GENESIS_HASH);
        event.setPreviousHash(prevHash);
        event.setCurrentHash(calculateHash(event));

        AuditLog savedEvent = repository.save(event);

        // Scenario C: Check for compliance anomalies (using try-catch so it doesn't fail the transaction)
        try {
            complianceService.analyzeForAnomalies(savedEvent);
        } catch (Exception e) {
            System.err.println("Failed to run anomaly analysis: " + e.getMessage());
        }

        return savedEvent;
    }

    public Page<AuditLog> queryEvents(String actorId, String eventType, String resourceType,
                                      String resourceId, Instant from, Instant to, Pageable pageable) {
        return repository.findAll((Specification<AuditLog>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("isArchived"), false));

            if (actorId != null) predicates.add(cb.equal(root.get("actorId"), actorId));
            if (eventType != null) predicates.add(cb.equal(root.get("eventType"), eventType));
            if (resourceType != null) predicates.add(cb.equal(root.get("resourceType"), resourceType));
            if (resourceId != null) predicates.add(cb.equal(root.get("resourceId"), resourceId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));

            return cb.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }

    public Map<String, Object> verifyChain() {
        Map<String, Object> report = new HashMap<>();
        List<AuditLog> chain = repository.findAllByOrderByIdAsc();
        String expectedPrevHash = GENESIS_HASH;

        for (AuditLog record : chain) {
            if (!record.isRedacted()) {
                if (!calculateHash(record).equals(record.getCurrentHash())) {
                    report.put("intact", false);
                    report.put("brokenRecordId", record.getId());
                    report.put("violationType", "MISMATCHED_CONTENT_HASH");
                    return report;
                }
            }

            if (!record.getPreviousHash().equals(expectedPrevHash)) {
                report.put("intact", false);
                report.put("brokenRecordId", record.getId());
                report.put("violationType", "PREVIOUS_HASH_MISMATCH");
                return report;
            }

            expectedPrevHash = record.getCurrentHash();
        }

        report.put("intact", true);
        return report;
    }

    @Transactional
    public int archiveOldRecords(Instant beforeDate) {
        List<AuditLog> oldRecords = repository.findByTimestampBeforeAndIsArchivedFalse(beforeDate);
        for (AuditLog record : oldRecords) {
            record.setArchived(true);
        }
        repository.saveAll(oldRecords);
        return oldRecords.size();
    }

    @Transactional
    public AuditLog redactRecord(Long id) {
        AuditLog record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        record.setPayload(REDACTED_PAYLOAD_FLAG);
        record.setRedacted(true);
        return repository.save(record);
    }

    public ExportBundle exportRecords(String actorId, String resourceId) {
        List<AuditLog> records = new ArrayList<>();
        if (actorId != null) {
            records = repository.findByActorIdOrderByIdAsc(actorId);
        } else if (resourceId != null) {
            records = repository.findByResourceIdOrderByIdAsc(resourceId);
        }

        StringBuilder combinedHashes = new StringBuilder();
        for (AuditLog record : records) {
            combinedHashes.append(record.getCurrentHash());
        }
        String bundleSignature = generateRawHash(combinedHashes.toString());

        Map<String, String> metadata = new HashMap<>();
        metadata.put("totalRecords", String.valueOf(records.size()));
        metadata.put("exportTimestamp", Instant.now().toString());

        return new ExportBundle(records, metadata, bundleSignature);
    }

    private String calculateHash(AuditLog log) {
        String dataToHash = String.format("%s|%s|%s|%s|%s|%s|%s",
                log.getEventType(), log.getActorId(), log.getResourceType(),
                log.getResourceId(), log.getPayload(), log.getTimestamp().toString(),
                log.getPreviousHash());
        return generateRawHash(dataToHash);
    }

    private String generateRawHash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 system environment fault", e);
        }
    }
}