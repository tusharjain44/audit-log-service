# AI Usage Log

## Collaboration Model
Used Gemini as an interactive coding assistant for architecture validation, security configuration, and test generation. 

## Prompt Transcripts (Samples)
* "How do I serialize concurrent writes in a Spring Boot JPA service to prevent a race condition on a cryptographic hash chain?"
* "Generate a MockMvc test suite that verifies HTTP Basic Authentication and RBAC roles (ADMIN vs USER) for Spring Boot 3.2."
* "What is the correct JPQL query to aggregate event counts by type without loading all entities into memory?"

## Reused Material & Verification
* Spring Security filter chain configurations were adapted from Spring documentation via AI suggestions.
* Tested thoroughly via `mvn clean test` combining unit, concurrency, and MockMvc integration tests.

## Limitations & Architectural Trade-offs
* **Scalability & Concurrency (ARCH-04):** The current tamper-evident chain uses a synchronized block and JPA Pessimistic Write locks. This provides strict serialization for a single JVM. For multi-node distributed scale, this must be upgraded to a distributed lock (e.g., Redis or Zookeeper) and a durable external chain head anchor.
* **Infrastructure Security:** TLS termination and strict rate-limiting are explicitly deferred to the deployment infrastructure layer (API Gateway).

* In-memory file H2 database used for prototype; production requires external Postgres/MySQL.
* Full chain verification is currently linear ($O(n)$) and will require batching logic at production scale.

* **Infrastructure Security (SEC-06, SEC-09):** TLS termination, strict CORS policies, and rate-limiting are explicitly deferred to the deployment infrastructure layer (e.g., API Gateway / Load Balancer) and are not embedded in this application prototype.


## Prompt-to-Change Traceability Map
* **Prompt:** "How do I serialize concurrent writes..." -> **Commit/Change:** Added TransactionTemplate and @Lock(PESSIMISTIC_WRITE) in commit 7ce90f735d82c11d5c7dccba938b27b0323a9d1d.
* **Prompt:** "Generate a MockMvc test suite..." -> **Commit/Change:** Created AuditLogControllerTest.java enforcing RBAC in commit 7ce90f735d82c11d5c7dccba938b27b0323a9d1d.
* **Prompt:** "What is the correct JPQL query..." -> **Commit/Change:** Optimized generateReport in ComplianceService in commit 7ce90f735d82c11d5c7dccba938b27b0323a9d1d.

