package io.temporal.workshops.springboot.interceptor.localactivity.auth;

import io.temporal.api.common.v1.Payloads;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.history.v1.HistoryEvent;
import io.temporal.api.history.v1.MarkerRecordedEventAttributes;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryRequest;
import io.temporal.api.workflowservice.v1.GetWorkflowExecutionHistoryResponse;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static io.temporal.workshops.springboot.interceptor.localactivity.auth.StarterRunner.TASK_QUEUE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles({"test", "interceptor-localactivity-auth"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext
public class HelloInterceptorWorkflowSpringBootTest {

    @Autowired
    ConfigurableApplicationContext applicationContext;

    @Autowired
    WorkflowClient workflowClient;

    @BeforeEach
    void setUp() {
        applicationContext.start();
    }

    @Test
    @Timeout(10)
    public void testHelloWithExpiredToken() {
        // Simulate an expired token — the interceptor should catch the
        // TokenExpired failure, call regenerateAuthToken(), refresh the
        // MDC entry, then retry the activity transparently
        MDC.put("x-auth-jwt-token", "expired-token");

        String workflowId = "interceptor-localactivity-auth" + System.currentTimeMillis();
        final HelloInterceptorWorkflow workflow = workflowClient.newWorkflowStub(
                HelloInterceptorWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setWorkflowId(workflowId)
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        final String result = workflow.sayHello("World");
        assertEquals("Hello, World!", result);



        //inspect the workflow history to verify the local activity was invoked
        final GetWorkflowExecutionHistoryResponse workflowHistory = workflowClient.getWorkflowServiceStubs().blockingStub().getWorkflowExecutionHistory(GetWorkflowExecutionHistoryRequest.newBuilder()
                .setNamespace(workflowClient.getOptions().getNamespace())
                .setExecution(WorkflowExecution.newBuilder().setWorkflowId(workflowId).build())
                .build());

        final HistoryEvent localActivity = workflowHistory.getHistory()
                .getEventsList().stream().filter(event -> {
                    MarkerRecordedEventAttributes la = event.getMarkerRecordedEventAttributes();
                    if (la.getMarkerName().equals("LocalActivity")) {
                        return true;
                    }
                    return false;
                })
                .findFirst().orElseThrow();


        final Payloads payloads = localActivity.getMarkerRecordedEventAttributes().getDetailsMap().get("type");

        final String activityName = io.temporal.common.converter.DataConverter
                .getDefaultInstance()
                .fromPayload(payloads.getPayloads(0), String.class, String.class);

        assertEquals("RegenerateAuthToken", activityName);
    }
}