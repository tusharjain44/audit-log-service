# Claim to Evidence Mapping

| Claim / Feature | Source Implementation | Test Evidence |
| :--- | :--- | :--- |
| **Tamper & Redaction Integrity** | `AuditLogService.verifyChain()`, `AuditLogService.redactRecord()` | `testRedactionReceiptPreventsBypass()`, `testTamperDetectionCatchesModifiedPayload()` |
| **Cross-Actor Isolation** | `AuditLogController.queryEvents()` | `testCrossActorIsolationBlocksAccess()` |
| **Replay Attack Prevention** | `AuditLogController.createEvent()` | `testIdempotencyBlocksReplayAttacks()` |
| **Concurrency Serialization** | `AuditLogService.createEvent()` | `testConcurrentAppendsDoNotFractureChain()` |
| **RBAC & Authentication** | `SecurityConfig.java` | `whenUnauthenticated_thenReturns401()`, `whenUserAccessesAdminRoute_thenReturns403()` |
| **Input Validation (DTOs)** | `AuditLogRequest.java`, `GlobalExceptionHandler.java` | `testMalformedPaginationReturns400()`, `whenInvalidPayload_thenReturns400()` |
| **Transaction Rollback** | `AuditLogService.createEvent()` | `testPersistenceFailureRollsBack()` |
