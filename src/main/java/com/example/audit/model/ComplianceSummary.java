package com.example.audit.model;

import java.time.Instant;
import java.util.Map;

public class ComplianceSummary {
    private Instant periodStart;
    private Instant periodEnd;
    private long totalEvents;
    private Map<String, Long> eventsByType;

    public ComplianceSummary() {}

    public ComplianceSummary(Instant periodStart, Instant periodEnd, long totalEvents, Map<String, Long> eventsByType) {
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.totalEvents = totalEvents;
        this.eventsByType = eventsByType;
    }

    public Instant getPeriodStart() { return periodStart; }
    public void setPeriodStart(Instant periodStart) { this.periodStart = periodStart; }

    public Instant getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(Instant periodEnd) { this.periodEnd = periodEnd; }

    public long getTotalEvents() { return totalEvents; }
    public void setTotalEvents(long totalEvents) { this.totalEvents = totalEvents; }

    public Map<String, Long> getEventsByType() { return eventsByType; }
    public void setEventsByType(Map<String, Long> eventsByType) { this.eventsByType = eventsByType; }
}