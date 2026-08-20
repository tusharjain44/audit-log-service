# Audit Log Service (Enterprise Edition)

## Architecture & Security Specifications
This service exposes a secure, paginated REST API for managing tamper-evident audit logs with enterprise-grade cryptographic controls.

### Authentication, Identity & RBAC (SEC-02, SEC-03, SEC-05)
* **Persistent Identity Store:** In-memory users have been replaced with a managed database-backed identity store (`UserAccountRepository`) using BCrypt password hashing.
* **Fail-Closed Startup:** The application enforces strict secret validation on startup. If required environment variables (`ADMIN_PASSWORD`, `USER_PASSWORD`) are missing, the application halts immediately.
* **Anti-Spoofing & Actor Isolation:** Actor IDs are cryptographically bound to the authenticated `Principal` token on `POST /events`. Client-supplied actor IDs in request bodies are ignored. Cross-actor isolation is strictly enforced (users can only access their own records unless they possess `ROLE_ADMIN`).

### Idempotency & Replay Protection (SEC-06)
* **Durable Idempotency Store:** Replay attacks and duplicate submissions are blocked using a database-backed table (`idempotency_keys`) with unique constraints, actor scoping, and payload hashes.

### Cryptographic Integrity & Re-anchoring (ARCH-02, SEC-07)
* **HMAC Signatures:** Redaction receipts and export bundles utilize `HmacSHA256` keyed cryptographic signatures rather than raw hashes.
* **Redaction Re-anchoring:** Redacted records maintain chain continuity via an immutable `originalHash` reference, ensuring tampering checks remain mathematically valid.
