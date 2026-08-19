# Claim-to-Evidence Mapping Matrix

| Claim / Requirement | Architectural Implementation | Source File Location | Verification / Test Evidence |
| :--- | :--- | :--- | :--- |
| **Tamper-Evident Hash Chain** | SHA-256 chaining with previous/current hash linkage | `AuditLogService.java`, `AuditLog.java` | `AuditLogServiceTest.java` |
| **Structured Redaction** | Flag-based logical redaction keeping chain intact | `AuditLogService.java` (`redactRecord`) | Controller & Service integration |
| **Retention Policy** | Soft-deletion using `isArchived` flag | `AuditLogService.java` (`archiveOldRecords`) | Repository query filters |
| **Compliance & Anomaly Detection** | Rolling window actor event frequency check | `ComplianceService.java` | Console logger triggers |
| **Security & Authentication** | HTTP Basic Authentication protecting all routes | `SecurityConfig.java` | Secured endpoint filter chain |
