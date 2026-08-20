package com.example.audit.repository;

import com.example.audit.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;

public interface IdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {
    void deleteByCreatedAtBefore(Instant expiryDate);
}