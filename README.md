# Audit Log Service

## API Specification & Error Contract
This service exposes a secure, paginated REST API for managing tamper-evident audit logs.

### Authentication & Authorization
* **Security:** HTTP Basic Authentication is enforced on all routes.
* **Roles:** `USER` can append and read active logs. `ADMIN` is required for redaction, archival, verification, and compliance exports.

### Endpoints (v1)
* `POST /audit/events` - Append a new audit event (Requires DTO with eventType, actorId, resourceType, resourceId, payload).
* `GET /audit/events` - Query events with pagination (`page`, `size` max 100) and time bounds.
* `GET /audit/verify` - [ADMIN] Run a full cryptographic chain verification.
* `POST /audit/events/{id}/redact` - [ADMIN] Logically redact a sensitive payload without breaking the chain.
* `DELETE /audit/archive` - [ADMIN] Soft-delete records older than a specific date.
* `GET /audit/export` - [ADMIN] Generate a cryptographically signed subset of records.
* `GET /audit/compliance/report` - [ADMIN] Generate an aggregated frequency report.

### Error Schema
All validation and operational errors return a structured JSON response (HTTP 400 or 500) rather than generic stack traces, managed via `GlobalExceptionHandler`:
{
  "actorId": "Actor ID cannot be blank",
  "error": "Internal Server Error"
}

### Operational Readiness
* **Metrics & Health:** Available via Spring Boot Actuator (`/actuator/health`).
* **Retention:** An automated scheduler archives records older than 365 days every night at midnight.
