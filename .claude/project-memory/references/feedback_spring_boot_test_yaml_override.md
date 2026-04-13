---
name: "Spring Boot Test YAML override pitfall"
description: "src/test/resources/application.yaml overwrites main config entirely — use application-test.yaml with @ActiveProfiles instead"
type: feedback
---

# Spring Boot Test YAML override pitfall

When writing `@SpringBootTest` tests for
Temporal workflows, never place test config in
`src/test/resources/application.yaml` — it
completely replaces the main
`src/main/resources/application.yaml`, losing
settings like `workersAutoDiscovery.packages`.
Without auto-discovery, no workers are created
and workflow executions hang forever.

**Why:** Spring Boot merges profile-specific
files (`application-{profile}.yaml`) on top of
the base `application.yaml`, but a
`src/test/resources/application.yaml` fully
overrides the one in `src/main/resources/`.

**How to apply:** use
`src/test/resources/application-test.yaml` with
only the test-specific overrides (e.g.
`spring.temporal.test-server.enabled: true`),
and activate it with
`@ActiveProfiles("test")` on the test class.
This extends the main config instead of
replacing it.
