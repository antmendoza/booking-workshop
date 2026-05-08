package io.temporal.app.domain.workflows.greeting;

import io.temporal.app.domain.integrations.GreetingActivity;
import io.temporal.app.domain.messages.Name;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowFailedException;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GreetingWorkflowTest {

    private static final String TASK_QUEUE = "SagaTaskQueue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        client = testEnv.getWorkflowClient();

        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
        worker.registerActivitiesImplementations(new GreetingActivity() {
            @Override
            public String greet1(Name name) {
                if ("Fail-1".equals(name.getFirstName())) {
                    throw new RuntimeException("Greet1 simulated failure");
                }
                return "Hello " + name.getFirstName() + " " + name.getLastName() + "-1";
            }
            @Override
            public String greet2(Name name) {
                if ("Fail-2".equals(name.getFirstName())) {
                    throw new RuntimeException("Greet2 simulated failure");
                }
                return "Hello " + name.getFirstName() + " " + name.getLastName() + "-2";
            }
            @Override
            public String greet3(Name name) {
                if ("Fail-3".equals(name.getFirstName())) {
                    throw new RuntimeException("Greet3 simulated failure");
                }
                return "Hello " + name.getFirstName() + " " + name.getLastName() + "-3";
            }
            @Override
            public void compensate1(Name name) {}
            @Override
            public void compensate2(Name name) {}
            @Override
            public void compensate3(Name name) {}
        });

        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void sayHello_happyPath_returnsChainedGreeting() {
        var workflow = client.newWorkflowStub(
                GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-happy")
                        .build());

        String result = workflow.sayHello(new Name("Donald Forbes"));

        assertEquals("Hello Donald Forbes-1-2-3", result);
    }

    @Test
    void sayHello_failAt1_workflowFails() {
        var workflow = client.newWorkflowStub(
                GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail1")
                        .build());

        assertThrows(WorkflowFailedException.class,
                () -> workflow.sayHello(new Name("Fail-1 Forbes")));
    }

    @Test
    void sayHello_failAt2_workflowFails() {
        var workflow = client.newWorkflowStub(
                GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail2")
                        .build());

        assertThrows(WorkflowFailedException.class,
                () -> workflow.sayHello(new Name("Fail-2 Forbes")));
    }

    @Test
    void sayHello_failAt3_workflowFails() {
        var workflow = client.newWorkflowStub(
                GreetingWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("test-fail3")
                        .build());

        assertThrows(WorkflowFailedException.class,
                () -> workflow.sayHello(new Name("Fail-3 Forbes")));
    }
}
