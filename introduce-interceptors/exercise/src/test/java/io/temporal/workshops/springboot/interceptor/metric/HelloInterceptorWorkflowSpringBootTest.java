package io.temporal.workshops.springboot.interceptor.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static io.temporal.workshops.springboot.interceptor.metric.StarterRunner.TASK_QUEUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles({"test", "interceptor-metric"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
public class HelloInterceptorWorkflowSpringBootTest {

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    WorkflowClient workflowClient;

    @Autowired
    MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        applicationContext.start();
    }

    @Test
    @Timeout(10)
    public void testHello() throws ExecutionException, InterruptedException {

        //Complete when the metric is added, so we can assert the value
        final CompletableFuture<Void> activityRetryAdded = new CompletableFuture<>();
        String activityRetryMetric = "activity_retry";
        CompletableFuture.runAsync(() -> meterRegistry.config().onMeterAdded(meter -> {
            if (activityRetryMetric.equals(meter.getId().getName())) {
                activityRetryAdded.complete(null);
            }
        }));


        final HelloInterceptorWorkflow workflow = workflowClient.newWorkflowStub(
                HelloInterceptorWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        final String result = workflow.sayHello("World");
        assertEquals("Hello, World!", result);


        //Wait for the metric to be added
        activityRetryAdded.get();
        double totalRetryCount = meterRegistry.find(activityRetryMetric)
                .counters()
                .stream()
                .mapToDouble(Counter::count)
                .sum();
        assertEquals(5, totalRetryCount);

    }

}
