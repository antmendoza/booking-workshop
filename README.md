# Temporal Booking Workshop

A Spring Boot application for running Temporal workers.

## Prerequisites

- Java 21
- Temporal server running locally on `127.0.0.1:7233`

Start a local Temporal server with the [Temporal CLI](https://docs.temporal.io/cli):

```bash
temporal server start-dev
```

## Running the Application

The application starts with no workers running by default.

```bash
./mvnw spring-boot:run
```

### Running with the Hello World Sample

Start the application with the `workflow-hello` profile to register a worker and automatically execute a sample `HelloWorldWorkflow` on startup.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=workflow-hello
```

You can observe the execution in the [Temporal Web UI](http://localhost:8233).

See [application-workflow-hello.yml](src/main/resources/application-workflow-hello.yml) for worker configuration.

## Metrics

The application includes Spring Boot Actuator and Micrometer with a Prometheus registry.

Metrics are available at [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

## Running Tests

```bash
./mvnw test
```

Tests use `TestWorkflowEnvironment` for fast, in-process execution without a live Temporal server.
