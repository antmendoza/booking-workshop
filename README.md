# booking-workshop

[![Build](https://github.com/antmendoza/booking-workshop/actions/workflows/build.yml/badge.svg)](https://github.com/antmendoza/booking-workshop/actions/workflows/build.yml)
![Java 21](https://img.shields.io/badge/Java-21-blue)
![Spring Boot 4.0](https://img.shields.io/badge/Spring%20Boot-4.0-green)

Hands-on workshop teaching Temporal workflow
patterns with Spring Boot. Designed for
developers learning to build resilient,
distributed applications using the Temporal SDK.
Delivered as a guided, multi-exercise training
session.

## Features

- Progressive exercises from basic workflows
  to advanced patterns
- Each exercise includes instructions and a
  reference solution
- Spring Boot + Temporal SDK integration
- Covers interceptors, auth propagation, testing,
  worker versioning, saga pattern, metrics, and DSL

## Prerequisites

- Java 21
- [Temporal CLI](https://docs.temporal.io/cli)

Start a local Temporal server:

```bash
temporal server start-dev
```

The server listens on `127.0.0.1:7233` with a
web UI at `http://localhost:8233`.

## Getting started

Each exercise lives in its own directory with an
`exercise/` subfolder (instructions) and a
`solution/` subfolder (reference implementation).

To build and run an exercise that has a `pom.xml`:

```bash
cd <exercise>/exercise   # or solution
./mvnw spring-boot:run
```

To run tests:

```bash
./mvnw test
```

## Workshop agenda

| #  | Exercise | Topic |
|----|----------|-------|
| 1  | [run-a-simple-workflow](run-a-simple-workflow/exercise/README.md) | Run a simple workflow |
| 2  | [introduce-interceptors](introduce-interceptors/exercise/README.md) | Custom retry metrics with interceptors |
| 3  | [use-interceptor-to-handle-auth-failure](use-interceptor-to-handle-auth-failure/exercise/README.md) | Auth failure handling via interceptors |
| 4  | [applying-best-practices](applying-best-practices/exercise/README.md) | Applying best practices |
| 5  | [understand-temporal-integration-with-spring-boot](understand-temporal-integration-with-spring-boot/exercise/README.md) | Spring Boot integration |
| 6  | [testing](testing/exercise/README.md) | Unit testing and replay testing |
| 7  | [worker-versioning](worker-versioning/exercise/README.md) | Worker versioning and migration |
| 8  | [priority-and-fairness](priority-and-fairness/exercise/README.md) | Priority and fair share processing |
| 9  | [saga-pattern-implementation](saga-pattern-implementation/exercise/README.md) | Saga pattern with compensation |
| 10 | [understanding-metrics](understanding-metrics/exercise/README.md) | Understanding metrics |
| 11 | [dynamic-workflows-and-dsl](dynamic-workflows-and-dsl/exercise/README.md) | Dynamic workflows and DSL |

## License

Apache-2.0 — see [LICENSE](LICENSE) for details.
