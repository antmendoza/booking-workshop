package io.temporal.workshops.springboot.interceptor.localactivity.auth;


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
        worker.registerActivitiesImplementations( new HelloActivityInterceptor() {

            @Override
            public String one(String name) {
                return "Hello " + name + " from mocked activity";
            }

            @Override
            public String two(String name) {
                return "";
            }

            @Override
            public String three(String name) {
                return "";
            }

            @Override
            public String regenerateAuthToken() {
                return "";
            }
        });

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
