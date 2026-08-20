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

## Limitations Inventory
* In-memory file H2 database used for prototype; production requires external Postgres/MySQL.
* Full chain verification is currently linear ($O(n)$) and will require batching logic at production scale.

* **Infrastructure Security (SEC-06, SEC-09):** TLS termination, strict CORS policies, and rate-limiting are explicitly deferred to the deployment infrastructure layer (e.g., API Gateway / Load Balancer) and are not embedded in this application prototype.

