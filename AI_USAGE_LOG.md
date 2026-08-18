# AI Usage Log

## Overview
This log documents the use of AI tools (GitHub Copilot and interactive AI collaboration) during the development of the Audit Log Service assessment. All code generated via AI was reviewed, tested, integrated, and validated locally.

## Development Log

### Scenario A: Tamper-Evident Engine
* **AI Assistance:** Prompted for SHA-256 cryptographic hashing logic linking `previousHash` to `currentHash` with a Genesis block fallback.
* **Review & Verification:** Verified that modifying historical database rows triggers a mismatch during `GET /audit/verify`. Handled database schema adjustments manually on Windows.

### Scenario B: Retention, Redaction & Bulk Export
* **AI Assistance:** Requested DTO structural outlines for `ExportBundle` and method stubs for archiving and flag-based redaction.
* **Review & Verification:** Implemented strict flag-based logical redaction (`isRedacted = true`) ensuring the hash chain verification engine skips content recomputation for redacted events while enforcing structural chain continuity.

### Scenario C: Compliance Reporting & Alerting
* **AI Assistance:** Generated code structures for compliance reporting summaries and high-frequency anomaly detection heuristics.
* **Review & Verification:** Validated compliance aggregation ranges via HTTP queries and confirmed background thread logging triggers correctly upon rapid event generation.