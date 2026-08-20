package com.example.audit.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys", uniqueConstraints = {@UniqueConstraint(columnNames = {"idempotencyKey", "actorId"})})
public class IdempotencyRecord {
    @Id 
    private String idempotencyKey;
    private String actorId;
    private String payloadHash;
    private Instant createdAt;

    public IdempotencyRecord() {}

    public IdempotencyRecord(String idempotencyKey, String actorId, String payloadHash, Instant createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.actorId = actorId;
        this.payloadHash = payloadHash;
        this.createdAt = createdAt;
    }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }

    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}