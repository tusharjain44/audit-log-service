package com.example.audit;

import com.example.audit.model.IdempotencyRecord;
import com.example.audit.model.UserAccount;
import com.example.audit.repository.UserAccountRepository;
import com.example.audit.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class EnterpriseSecurityTest {

    @Test
    void testUserDetailsServiceLoadsValidUser() {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        UserAccount account = new UserAccount();
        account.setUsername("testuser");
        account.setPassword("encodedpass");
        account.setRole("USER");
        
        when(repo.findById("testuser")).thenReturn(Optional.of(account));
        
        CustomUserDetailsService service = new CustomUserDetailsService(repo);
        UserDetails details = service.loadUserByUsername("testuser");
        
        assertEquals("testuser", details.getUsername());
    }

    @Test
    void testUserDetailsServiceRejectsInvalidUser() {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        when(repo.findById(any())).thenReturn(Optional.empty());
        
        CustomUserDetailsService service = new CustomUserDetailsService(repo);
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("ghost"));
    }

    @Test
    void modelGetterSettersForCoverage() {
        Instant now = Instant.now();
        IdempotencyRecord rec = new IdempotencyRecord("key", "actor", "hash", now);
        assertEquals("key", rec.getIdempotencyKey());
        assertEquals("actor", rec.getActorId());
        assertEquals("hash", rec.getPayloadHash());
        assertEquals(now, rec.getCreatedAt());

        rec.setIdempotencyKey("new-key");
        rec.setActorId("new-actor");
        rec.setPayloadHash("new-hash");
        rec.setCreatedAt(Instant.MAX);
        
        assertEquals("new-key", rec.getIdempotencyKey());
        assertEquals("new-actor", rec.getActorId());
        assertEquals("new-hash", rec.getPayloadHash());
        assertEquals(Instant.MAX, rec.getCreatedAt());

        UserAccount acc = new UserAccount();
        acc.setUsername("u");
        acc.setPassword("p");
        acc.setRole("R");
        assertEquals("u", acc.getUsername());
        assertEquals("p", acc.getPassword());
        assertEquals("R", acc.getRole());
    }
}