package io.temporal.workflow.interceptor.metric;


import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloInterceptorWorkflowTest {

    private static final String TASK_QUEUE = "HelloSampleTaskQueue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        client = testEnv.getWorkflowClient();

        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(HelloInterceptorWorkflowImpl.class);
        worker.registerActivitiesImplementations((HelloActivityInterceptor) name -> "Hello " + name + " from mocked activity");

        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void sayHello_returnsGreeting() {
        HelloInterceptorWorkflow workflow = client.newWorkflowStub(
                HelloInterceptorWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .build()
        );

        String result = workflow.sayHello("World");

        assertEquals("Hello World from mocked activity", result);
    }
}
