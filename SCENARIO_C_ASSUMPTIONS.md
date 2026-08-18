# Scenario C: Compliance Reporting & Ambiguity Handling

## 1. Ambiguity & Assumptions
* **Ambiguity:** The prompt specifies compliance reporting and alerting but leaves the exact threshold definitions and reporting intervals open to interpretation.
* **Assumption Made:** For the anomaly detection heuristic, I assumed that an actor executing more than **5 actions within a 1-minute rolling window** represents a potential automated script flood or brute-force attempt. For reporting, compliance summaries default to aggregate counts grouped by event types within user-specified ISO-8601 timestamp bounds.

## 2. Trade-offs & Production Readiness
* **Asynchronous Alerting Trade-off:** The anomaly detection currently executes inline within a `try-catch` block inside the event creation transaction. In a massive enterprise system, this should be offloaded to an asynchronous message broker (e.g., Apache Kafka or RabbitMQ) to prevent analytics bottlenecks from impacting core audit-write latencies. However, for a self-contained Spring Boot prototype, an inline guarded execution guarantees zero dropped alerts without requiring external infrastructure containers.