package io.temporal.app.domain.workflows.hello;

import io.temporal.app.domain.integrations.HelloActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorldWorkflowTest {

    private static final String TASK_QUEUE = "HelloSampleTaskQueue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        client = testEnv.getWorkflowClient();

        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                HelloWorldWorkflowImpl.class);
        worker.registerActivitiesImplementations(
                new HelloActivity() {
                    @Override
                    public String greet(Name name) {
                        return "Hello " + name.getName() + " from mocked activity";
                    }

                    @Override
                    public String getWorkerVersion() {
                        return "1.0-test";
                    }
                });
        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void sayHello_loopsAndReturnsLastGreeting() {
        var workflow = client.newWorkflowStub(
                HelloWorldWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-say-hello")
                        .build());

        // Simulated time in TestWorkflowEnvironment advances instantly
        String result = workflow.sayHello(new Name("World"));

        assertEquals("Hello World from mocked activity", result);
    }

    @Test
    void greet_returnsGreetingImmediately() {
        var workflow = client.newWorkflowStub(
                HelloWorldWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-greet")
                        .build());

        // Start workflow asynchronously so it keeps running while we send the update
        WorkflowClient.start(workflow::sayHello, new Name("World"));

        // Send the greet update — runs one activity call and returns immediately
        String result = workflow.greet(new Name("Alice"));

        assertEquals("Hello Alice from mocked activity", result);
    }
}
