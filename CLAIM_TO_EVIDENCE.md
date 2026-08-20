# Claim to Evidence Mapping

| Claim / Feature | Source Implementation | Test Evidence |
| :--- | :--- | :--- |
| **Fail-Closed Secret Validation** | `SecurityStartupConfig.java` | `testMissingSecretsThrowsExceptionOnStartup()` |
| **DB-Backed Identity Store** | `CustomUserDetailsService.java`, `UserAccount.java` | `testUserDetailsServiceLoadsValidUser()`, `testUserDetailsServiceRejectsInvalidUser()` |
| **Tamper & Redaction Re-anchoring** | `AuditLogService.verifyChain()`, `AuditLogService.redactRecord()` | `testChainVerificationFailsOnCorruptedHash()`, `testRedactionReceiptPreventsBypass()` |
| **Actor Anti-Spoofing & Isolation** | `AuditLogController.createEvent()`, `AuditLogController.queryEvents()` | `testActorSpoofingIsPrevented()`, `testCrossActorIsolationBlocksAccess()` |
| **Durable Idempotency / Replay Guard** | `IdempotencyRecord.java`, `AuditLogController.createEvent()` | `testIdempotencyBlocksReplayAttacks()` |
| **Transaction Rollback & Concurrency** | `AuditLogService.createEvent()` | `testPersistenceFailureRollsBack()`, `testConcurrentAppendsDoNotFractureChain()` |
