package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void testTamperDetectionCatchesModifiedPayload() {
        AuditLog log = new AuditLog();
        log.setEventType("LOGIN");
        log.setActorId("user-1");
        log.setResourceType("SYS");
        log.setResourceId("1");
        log.setPayload("valid data");
        AuditLog saved = auditLogService.createEvent(log);

        saved.setPayload("tampered data");
        auditLogRepository.save(saved);

        Map<String, Object> verification = auditLogService.verifyChain();
        assertFalse((Boolean) verification.get("intact"));
    }

    @Test
    void testRedactionReceiptPreventsBypass() {
        // 1. Create a legitimate record
        AuditLog log = new AuditLog();
        log.setEventType("LOGIN");
        log.setActorId("user-1");
        log.setResourceType("SYS");
        log.setResourceId("1");
        log.setPayload("sensitive data");
        AuditLog saved = auditLogService.createEvent(log);

        // 2. Redact it properly via the service
        AuditLog redacted = auditLogService.redactRecord(saved.getId());

        // 3. Chain should be intact right now
        assertTrue((Boolean) auditLogService.verifyChain().get("intact"));

        // 4. Simulate Database Attacker: Change payload and keep isRedacted = true
        redacted.setPayload("[MALICIOUS_TAMPER_EVASION]");
        auditLogRepository.save(redacted);

        // 5. Verification MUST fail now due to receipt mismatch (Proves SEC-10 & TEST-05)
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
            final int index = i;
            executorService.execute(() -> {
                try {
                    AuditLog log = new AuditLog();
                    log.setEventType("EVENT_" + index);
                    log.setActorId("user-1");
                    log.setResourceType("SYS");
                    log.setResourceId("RES");
                    log.setPayload("Data");
                    auditLogService.createEvent(log);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        assertEquals(10, auditLogRepository.count());
        Map<String, Object> verification = auditLogService.verifyChain();
        assertTrue((Boolean) verification.get("intact"), "Concurrent appends caused a hash race condition!");
    }
}