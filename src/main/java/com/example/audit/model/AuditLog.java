package com.example.audit.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_actor", columnList = "actorId"),
        @Index(name = "idx_resource", columnList = "resourceType,resourceId"),
        @Index(name = "idx_event_type", columnList = "eventType"),
        @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String eventType;
    @Column(nullable = false) private String actorId;
    @Column(nullable = false) private String resourceType;
    @Column(nullable = false) private String resourceId;
    @Column(columnDefinition = "TEXT", nullable = false) private String payload;
    @Column(nullable = false) private Instant timestamp;
    @Column(nullable = false, length = 64) private String currentHash;
    @Column(nullable = false, length = 64) private String previousHash;

    // Scenario B Additions
    @Column(nullable = false) private boolean isArchived = false;
    @Column(nullable = false) private boolean isRedacted = false;
    
    // Cryptographic proof of authorized redaction
    @Column(length = 64) private String redactionReceipt;

    public AuditLog() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getCurrentHash() { return currentHash; }
    public void setCurrentHash(String currentHash) { this.currentHash = currentHash; }
    
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }

    public boolean isArchived() { return isArchived; }
    public void setArchived(boolean archived) { this.isArchived = archived; }
    
    public boolean isRedacted() { return isRedacted; }
    public void setRedacted(boolean redacted) { this.isRedacted = redacted; }

    public String getRedactionReceipt() { return redactionReceipt; }
    public void setRedactionReceipt(String redactionReceipt) { this.redactionReceipt = redactionReceipt; }
}