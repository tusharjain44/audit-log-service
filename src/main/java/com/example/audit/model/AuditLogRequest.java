package com.example.audit.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuditLogRequest {
    @NotBlank(message = "Event type cannot be blank")
    @Size(max = 50, message = "Event type too long")
    private String eventType;

    @NotBlank(message = "Actor ID cannot be blank")
    @Size(max = 100, message = "Actor ID too long")
    private String actorId;

    @NotBlank(message = "Resource Type cannot be blank")
    @Size(max = 50, message = "Resource Type too long")
    private String resourceType;

    @NotBlank(message = "Resource ID cannot be blank")
    @Size(max = 100, message = "Resource ID too long")
    private String resourceId;

    @NotBlank(message = "Payload cannot be blank")
    @Size(max = 5000, message = "Payload exceeds maximum allowed size of 5000 characters")
    private String payload;

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
}