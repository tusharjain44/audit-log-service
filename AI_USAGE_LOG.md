# AI Usage & Architectural Log

## Iterative Hardening & Enterprise Enhancements
* **Identity Migration:** Transitioned from static in-memory user profiles to a managed relational schema (`UserAccount`) with fail-closed startup checks.
* **Idempotency Architecture:** Upgraded from a volatile JVM-local `ConcurrentHashMap` to a durable SQL table with unique constraints, payload hashing, and actor scoping.
* **Cryptographic Hardening:** Replaced unkeyed SHA-256 strings with `HmacSHA256` signatures and implemented hash re-anchoring for redacted log entries to preserve chain integrity.
* **Negative Security Matrix:** Added explicit test scenarios covering actor spoofing prevention, database-level replay blocking, startup failure modes, and hash corruption detection.
