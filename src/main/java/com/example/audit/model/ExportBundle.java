package com.example.audit.model;

import java.util.List;
import java.util.Map;

public class ExportBundle {

    private List<AuditLog> records;
    private Map<String, String> chainMetadata;
    private String bundleSignature;

    public ExportBundle() {}

    public ExportBundle(List<AuditLog> records, Map<String, String> chainMetadata, String bundleSignature) {
        this.records = records;
        this.chainMetadata = chainMetadata;
        this.bundleSignature = bundleSignature;
    }

    public List<AuditLog> getRecords() { return records; }
    public void setRecords(List<AuditLog> records) { this.records = records; }

    public Map<String, String> getChainMetadata() { return chainMetadata; }
    public void setChainMetadata(Map<String, String> chainMetadata) { this.chainMetadata = chainMetadata; }

    public String getBundleSignature() { return bundleSignature; }
    public void setBundleSignature(String bundleSignature) { this.bundleSignature = bundleSignature; }
}