package com.example.audit.controller;

import com.example.audit.model.AuditLog;
import com.example.audit.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private AuditLogRepository repository;

    @BeforeEach
    void setUp() { repository.deleteAll(); }

    @Test
    void whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get("/audit/events")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenUserAccessesAdminRoute_thenReturns403() throws Exception {
        mockMvc.perform(get("/audit/verify")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user-1", roles = "USER")
    void testCrossActorIsolationBlocksAccess() throws Exception {
        mockMvc.perform(get("/audit/events?actorId=user-2")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testMalformedPaginationReturns400() throws Exception {
        mockMvc.perform(get("/audit/events?page=-1&size=5000")).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testIdempotencyBlocksReplayAttacks() throws Exception {
        String payload = "{\"eventType\": \"LOGIN\", \"actorId\": \"u1\", \"resourceType\": \"SYS\", \"resourceId\": \"1\", \"payload\": \"data\"}";
        mockMvc.perform(post("/audit/events").header("Idempotency-Key", "tx-999").contentType(MediaType.APPLICATION_JSON).content(payload))
               .andExpect(status().isOk());
        mockMvc.perform(post("/audit/events").header("Idempotency-Key", "tx-999").contentType(MediaType.APPLICATION_JSON).content(payload))
               .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void fullAdminRouteCoverage() throws Exception {
        AuditLog log = new AuditLog();
        log.setEventType("TEST"); log.setActorId("admin"); log.setResourceType("SYS"); log.setResourceId("1");
        log.setPayload("data"); log.setTimestamp(Instant.now()); log.setCurrentHash("hash"); log.setPreviousHash("hash");
        log = repository.save(log);

        mockMvc.perform(get("/audit/verify")).andExpect(status().isOk());
        mockMvc.perform(post("/audit/events/" + log.getId() + "/redact")).andExpect(status().isOk());
        mockMvc.perform(delete("/audit/archive?beforeDate=" + Instant.now().toString())).andExpect(status().isOk());
        mockMvc.perform(get("/audit/export?actorId=admin")).andExpect(status().isOk());
        mockMvc.perform(get("/audit/compliance/report?start=2020-01-01T00:00:00Z&end=2030-01-01T00:00:00Z")).andExpect(status().isOk());
    }
}