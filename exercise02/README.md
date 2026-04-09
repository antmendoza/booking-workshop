# Temporal Booking Workshop

A Spring Boot application for running Temporal workers.

## Metrics

The application includes Spring Boot Actuator and Micrometer with a Prometheus registry.

Metrics are available at [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus)

### Running with the Interceptor Metric Sample

Start the application with the `interceptor-metric` profile to discover workflows and activities in `io.temporal.workflow.interceptor.metric` and register a worker
and automatically execute the workflow on startup.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=interceptor-metric
```

Query the metrics to see the activity retry count:

```bash
curl -s http://localhost:3030/actuator/prometheus | grep '^activity_retry'
```
Expected output:

```
activity_retry_total{activity_type="Greet",...,workflow_run_id="019d5920-4e44-76fd-9c44-f79a27b78b49",workflow_type="HelloInterceptorWorkflow"} 5.0

```

For a detailed see the [Interceptor Metric README](src/main/java/io/temporal/workflow/interceptor/metric/README.md).
