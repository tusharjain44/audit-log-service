package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.model.ExportBundle;
import com.example.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.example.audit.AuditLogApplication.class)
class AuditLogServiceTest {

    @Autowired private AuditLogService auditLogService;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ComplianceService complianceService;

    @BeforeEach
    void setUp() { auditLogRepository.deleteAll(); }

    @Test
    void testChainVerificationFailsOnCorruptedHash() {
        AuditLog saved = createTestEvent();
        saved.setCurrentHash("corrupted-hash");
        auditLogRepository.save(saved);
        Map<String, Object> verification = auditLogService.verifyChain();
        assertFalse((Boolean) verification.get("intact"));
        assertEquals("MISMATCHED_CONTENT_HASH", verification.get("violationType"));
    }

    @Test
    void testTamperDetectionCatchesModifiedPayload() {
        AuditLog saved = createTestEvent();
        saved.setPayload("tampered data");
        auditLogRepository.save(saved);
        assertFalse((Boolean) auditLogService.verifyChain().get("intact"));
    }

    @Test
    void testRedactionReceiptPreventsBypass() {
        AuditLog saved = createTestEvent();
        AuditLog redacted = auditLogService.redactRecord(saved.getId());
        assertTrue((Boolean) auditLogService.verifyChain().get("intact"));

        redacted.setPayload("[MALICIOUS_TAMPER_EVASION]");
        auditLogRepository.save(redacted);
        Map<String, Object> verification = auditLogService.verifyChain();
        assertFalse((Boolean) verification.get("intact"));
        assertEquals("MALICIOUS_REDACTION_PAYLOAD", verification.get("violationType"));
    }

    @Test
    void testConcurrentAppendsDoNotFractureChain() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try { createTestEvent(); } finally { latch.countDown(); }
            });
        }
        latch.await();
        assertTrue((Boolean) auditLogService.verifyChain().get("intact"));
    }

    @Test
    void testPersistenceFailureRollsBack() {
        AuditLog invalidLog = new AuditLog();
        assertThrows(Exception.class, () -> auditLogService.createEvent(invalidLog));
        assertEquals(0, auditLogRepository.count(), "Rollback failed; partial state persisted");
        assertTrue((Boolean) auditLogService.verifyChain().get("intact"), "Chain fractured during rollback");
    }

    @Test
    void testDedicatedCryptoBoundary() throws Exception {
        java.lang.reflect.Method hashMethod = AuditLogService.class.getDeclaredMethod("hashString", String.class);
        hashMethod.setAccessible(true);
        String hash1 = (String) hashMethod.invoke(auditLogService, "PAYLOAD_A");
        String hash2 = (String) hashMethod.invoke(auditLogService, "PAYLOAD_A");
        String hash3 = (String) hashMethod.invoke(auditLogService, "PAYLOAD_B");
        
        assertEquals(64, hash1.length());
        assertEquals(hash1, hash2, "Crypto hashing is not deterministic");
        assertNotEquals(hash1, hash3, "Crypto hashing produced a collision");
    }

    @Test
    void testQueryAndExportAndArchiveBranches() {
        AuditLog saved = createTestEvent();
        assertNotNull(auditLogService.queryEvents("user-1", "LOGIN", "SYS", "1", Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS), PageRequest.of(0, 10)));
        assertNotNull(auditLogService.queryEvents(null, null, null, null, null, null, PageRequest.of(0, 10)));

        ExportBundle bundle = auditLogService.exportRecords("user-1", null);
        assertNotNull(bundle.getBundleSignature());
        auditLogService.exportRecords(null, "1");
        
        assertNotNull(complianceService.generateReport(Instant.now().minus(1, ChronoUnit.DAYS), Instant.now().plus(1, ChronoUnit.DAYS)));

        auditLogService.automatedRetentionSweep();
        assertEquals(1, auditLogService.archiveOldRecords(Instant.now().plus(1, ChronoUnit.DAYS)));
    }

    private AuditLog createTestEvent() {
        AuditLog log = new AuditLog();
        log.setEventType("LOGIN"); log.setActorId("user-1"); log.setResourceType("SYS");
        log.setResourceId("1"); log.setPayload("valid data");
        return auditLogService.createEvent(log);
    }
}