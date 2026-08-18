package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
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
    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public AuditLog createEvent(AuditLog event) {
        // Truncate precision to ensure Java and H2 DB string representations match exactly
        event.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));

        Optional<AuditLog> lastRecord = repository.findTopByOrderByIdDesc();
        String prevHash = lastRecord.map(AuditLog::getCurrentHash).orElse(GENESIS_HASH);
        event.setPreviousHash(prevHash);

        event.setCurrentHash(calculateHash(event));
        return repository.save(event);
    }

    public Page<AuditLog> queryEvents(String actorId, String eventType, String resourceType,
                                      String resourceId, Instant from, Instant to, Pageable pageable) {
        return repository.findAll((Specification<AuditLog>) (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

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
            // 1. Verify content hasn't been changed directly in DB
            if (!calculateHash(record).equals(record.getCurrentHash())) {
                report.put("intact", false);
                report.put("brokenRecordId", record.getId());
                report.put("violationType", "MISMATCHED_CONTENT_HASH");
                return report;
            }

            // 2. Verify link broken chain
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

    private String calculateHash(AuditLog log) {
        // Enforce deterministic string tokenization for security
        String dataToHash = String.format("%s|%s|%s|%s|%s|%s|%s",
                log.getEventType(), log.getActorId(), log.getResourceType(),
                log.getResourceId(), log.getPayload(), log.getTimestamp().toString(),
                log.getPreviousHash());
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes(StandardCharsets.UTF_8));
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