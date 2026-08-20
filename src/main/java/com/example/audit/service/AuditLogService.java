package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.model.ExportBundle;
import com.example.audit.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.criteria.Predicate;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class AuditLogService {

    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";
    private final AuditLogRepository repository;
    private final ComplianceService complianceService;
    private final TransactionTemplate transactionTemplate;

    public AuditLogService(AuditLogRepository repository, ComplianceService complianceService, TransactionTemplate transactionTemplate) {
        this.repository = repository;
        this.complianceService = complianceService;
        this.transactionTemplate = transactionTemplate;
    }

    public AuditLog createEvent(AuditLog event) {
        synchronized (this) {
            AuditLog savedEvent = transactionTemplate.execute(status -> {
                event.setTimestamp(Instant.now().truncatedTo(ChronoUnit.MILLIS));
                Optional<AuditLog> lastRecord = repository.findTopByOrderByIdDesc();
                String prevHash = lastRecord.map(AuditLog::getCurrentHash).orElse(GENESIS_HASH);
                
                event.setPreviousHash(prevHash);
                event.setCurrentHash(calculateHash(event));
                
                return repository.saveAndFlush(event);
            });
            try {
                complianceService.analyzeForAnomalies(savedEvent);
            } catch (Exception e) {}
            return savedEvent;
        }
    }

    
    @Transactional(readOnly = true)
    public Map<String, Object> verifyChain() {
        List<AuditLog> allLogs = repository.findAllByOrderByIdAsc();
        Map<String, Object> result = new HashMap<>();
        String expectedPrevHash = GENESIS_HASH;
        
        for (AuditLog log : allLogs) {
            if (!log.getPreviousHash().equals(expectedPrevHash)) {
                result.put("intact", false); result.put("violationType", "BROKEN_LINK"); return result;
            }
            if (log.isRedacted()) {
                if (!"[REDACTED_FOR_PRIVACY]".equals(log.getPayload())) {
                    result.put("intact", false); result.put("violationType", "MALICIOUS_REDACTION_PAYLOAD"); return result;
                }
                if (!log.getCurrentHash().equals(calculateHash(log))) {
                    result.put("intact", false); result.put("violationType", "MISMATCHED_REDACTED_HASH"); return result;
                }
                expectedPrevHash = log.getOriginalHash(); // ARCH-02: Re-anchor the chain using original hash
            } else {
                if (!log.getCurrentHash().equals(calculateHash(log))) {
                    result.put("intact", false); result.put("violationType", "MISMATCHED_CONTENT_HASH"); return result;
                }
                expectedPrevHash = log.getCurrentHash();
            }
        }
        result.put("intact", true); return result;
    }
    @Transactional
    public AuditLog redactRecord(Long id) {
        AuditLog log = repository.findById(id).orElseThrow(() -> new RuntimeException("Record not found"));
        log.setOriginalHash(log.getCurrentHash());
        log.setRedacted(true);
        log.setPayload("[REDACTED_FOR_PRIVACY]");
        log.setCurrentHash(calculateHash(log)); // ARCH-02: Do not leave currentHash inconsistent
        return repository.save(log);
    }@Transactional(readOnly = true)
    public Page<AuditLog> queryEvents(String actorId, String eventType, String resourceType, String resourceId, Instant from, Instant to, Pageable pageable) {
        Specification<AuditLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actorId != null) predicates.add(cb.equal(root.get("actorId"), actorId));
            if (eventType != null) predicates.add(cb.equal(root.get("eventType"), eventType));
            if (resourceType != null) predicates.add(cb.equal(root.get("resourceType"), resourceType));
            if (resourceId != null) predicates.add(cb.equal(root.get("resourceId"), resourceId));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), to));
            predicates.add(cb.isFalse(root.get("isArchived")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable);
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void automatedRetentionSweep() {
        System.out.println("Running scheduled retention sweep...");
        archiveOldRecords(Instant.now().minus(365, ChronoUnit.DAYS));
    }

    @Transactional
    public int archiveOldRecords(Instant beforeDate) {
        List<AuditLog> oldRecords = repository.findByTimestampBeforeAndIsArchivedFalse(beforeDate);
        oldRecords.forEach(r -> r.setArchived(true));
        repository.saveAll(oldRecords);
        return oldRecords.size();
    }

    @Transactional(readOnly = true)
    public ExportBundle exportRecords(String actorId, String resourceId) {
        List<AuditLog> records = new ArrayList<>();
        if (actorId != null) records.addAll(repository.findByActorIdOrderByIdAsc(actorId));
        else if (resourceId != null) records.addAll(repository.findByResourceIdOrderByIdAsc(resourceId));
        
        ExportBundle bundle = new ExportBundle();
        bundle.setRecords(records);
        
        StringBuilder hashChain = new StringBuilder();
        for (AuditLog r : records) {
            hashChain.append(r.getCurrentHash());
        }
        bundle.setBundleSignature(generateHmacSignature(hashChain.toString()));
        return bundle;
    }

    private String calculateHash(AuditLog log) {
        String data = log.getPreviousHash() + log.getEventType() + log.getActorId() + 
                      log.getResourceType() + log.getResourceId() + log.getPayload() + log.getTimestamp();
        return hashString(data);
    }

    private String hashString(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed", e);
        }
    }

    private String generateHmacSignature(String data) {
        try {
            String secret = System.getenv().getOrDefault("HMAC_SECRET", "FallbackOnlyForTests123!@#");
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hmacBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC generation failed", e);
        }
    }
}