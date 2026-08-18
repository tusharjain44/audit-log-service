# Scenario A: Tamper-Evident Engine — Design & Assumptions

## 1. Cryptographic Architecture
* **Hash Chain Mechanics:** Each audit log entry is cryptographically bound to its predecessor by hashing its core fields concatenated with the `previousHash` of the preceding record.
* **Genesis Block:** For the initial record in the system (where no predecessor exists), a fixed genesis hash consisting of sixty-four zeros (`0000000000000000000000000000000000000000000000000000000000000000`) is used as the foundational anchor.

## 2. Assumptions & Ambiguities
* **Database Ordering:** We assume that database record IDs (`id`) increment monotonically and reliably represent chronological creation order. This allows the verification engine (`verifyChain()`) to safely traverse records sequentially from oldest to newest using ascending ID order.
* **Algorithm Choice:** Standard SHA-256 was chosen because it provides an optimal balance between cryptographic security and performance overhead suitable for high-throughput audit logging services.

## 3. Trade-offs & Limitations
* **Strict Linearity Trade-off:** A linear hash chain ensures absolute tamper-evidence (if any historical byte changes, all subsequent hashes break). However, it introduces a write bottleneck under extremely high concurrent database insertions because each write depends on the hash of the immediately preceding row. For a standalone prototype, database transactions handle this safely; enterprise scaling would require merkle-tree batching.