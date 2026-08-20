package com.example.audit.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenUnauthenticated_thenReturns401() throws Exception {
        mockMvc.perform(get("/audit/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenUserAccessesAdminRoute_thenReturns403() throws Exception {
        mockMvc.perform(get("/audit/verify"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAdminAccessesAdminRoute_thenReturns200() throws Exception {
        mockMvc.perform(get("/audit/verify"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenInvalidPayload_thenReturns400() throws Exception {
        String badPayload = "{\"eventType\": \"\", \"actorId\": \"user-1\"}"; // Missing required fields
        mockMvc.perform(post("/audit/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(badPayload))
                .andExpect(status().isBadRequest());
    }
}