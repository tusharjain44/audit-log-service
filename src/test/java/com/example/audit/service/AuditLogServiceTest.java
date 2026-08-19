package com.example.audit.service;

import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
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
    void testCreateEventAndGenesisHash() {
        AuditLog log = new AuditLog();
        log.setEventType("USER_LOGIN");
        log.setActorId("user-1");
        log.setResourceType("SESSION");
        log.setResourceId("session-1");
        log.setPayload("{\"status\":\"success\"}");

        AuditLog saved = auditLogService.createEvent(log);

        assertNotNull(saved.getId());
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", saved.getPreviousHash());
        assertNotNull(saved.getCurrentHash());
        
        var verification = auditLogService.verifyChain();
        assertTrue((Boolean) verification.get("intact"));
    }
}