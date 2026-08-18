# Scenario B: Redaction, Retention & Bulk Export — Design & Assumptions

## 1. Structured Redaction Strategy
* **Approach:** To comply with data privacy rights (such as GDPR right-to-be-forgotten) without invalidating the cryptographic ledger, we implement flag-based logical redaction (`isRedacted = true` and `payload` overridden with `[REDACTED_FOR_PRIVACY]`).
* **Verification Handling:** The verification engine dynamically checks the `isRedacted` flag. If true, it bypasses re-computing the individual content hash (which would naturally mismatch due to the redacted payload) while **strictly enforcing the `previousHash` chain link** to ensure structural timeline integrity remains intact.

## 2. Retention Policy (Archival vs. Deletion)
* **Approach:** Physical deletion of old logs would permanently fracture the cryptographic hash chain, rendering the remaining system un-verifiable. Therefore, we utilize soft-deletion via an `isArchived` flag.
* **Query Impact:** Standard transactional queries filter out archived records (`isArchived == false`), keeping active operational queries performant while preserving historical compliance data for auditing.

## 3. Bulk Export & Verification
* **Approach:** The bulk export compiles subset records into a self-contained `ExportBundle` DTO accompanied by a `bundleSignature`. This signature is generated via a rolling SHA-256 hash computed across the concatenated `currentHash` values of the exported subset, enabling independent third-party verification of the bundle's completeness.