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

        // Bypass service to simulate DB tampering
        saved.setPayload("tampered data");
        auditLogRepository.save(saved);

        Map<String, Object> verification = auditLogService.verifyChain();
        assertFalse((Boolean) verification.get("intact"));
    }

    @Test
    void testRedactionSkipsHashFailure() {
        AuditLog log = new AuditLog();
        log.setEventType("LOGIN");
        log.setActorId("user-1");
        log.setResourceType("SYS");
        log.setResourceId("1");
        log.setPayload("sensitive data");
        AuditLog saved = auditLogService.createEvent(log);

        auditLogService.redactRecord(saved.getId());

        // Chain should remain intact because redaction is a recognized structural state
        Map<String, Object> verification = auditLogService.verifyChain();
        assertTrue((Boolean) verification.get("intact"));
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