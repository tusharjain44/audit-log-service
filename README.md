# Audit Log Service

## API Specification & Error Contract
This service exposes a secure, paginated REST API for managing tamper-evident audit logs.

### Authentication & Authorization (RBAC & Isolation)
* **Security:** HTTP Basic Authentication is enforced on all routes with fail-closed secret injection.
* **Roles & Isolation:** `USER` can append and read *only* their own active logs (Cross-Actor Isolation enforced). `ADMIN` is required for redaction, archival, verification, and compliance exports.

### Endpoints (v1)
* `POST /audit/events` - Append a new event. (Requires `Idempotency-Key` header to prevent replay attacks).
* `GET /audit/events` - Query events with strict pagination bounds.
* `GET /audit/verify` - [ADMIN] Run full cryptographic chain and redaction receipt verification.
* `POST /audit/events/{id}/redact` - [ADMIN] Cryptographically redact a payload with an immutable receipt.
* `DELETE /audit/archive` - [ADMIN] Soft-delete old records.
* `GET /audit/export` - [ADMIN] Generate a cryptographically signed subset of records.
* `GET /audit/compliance/report` - [ADMIN] Generate an aggregated frequency report.

### Production Readiness & Rigor (Deployment)
While this prototype runs an embedded H2 database for testing, a hardened production profile (`application-prod.properties`) is provided. The production profile enforces:
* PostgreSQL dependency with `ddl-auto=validate` to prevent accidental schema corruption.
* TLS/HTTPS termination at the application tier.
* Strict PII sanitization in logs and dedicated `ADMIN_AUDIT` trails.
