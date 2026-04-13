package io.temporal.workshops.springboot.testing.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelloWorkflowMockitoTest {

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;
    private HelloActivity mockedActivity;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        client = testEnv.getWorkflowClient();

        mockedActivity = mock(HelloActivity.class);
        when(mockedActivity.greet(anyString()))
                .thenAnswer(inv -> "Mocked: Hello, "
                        + inv.getArgument(0) + "!");

        Worker worker = testEnv.newWorker(HelloWorkflow.TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                HelloWorkflowImpl.class);

        // Wrap the Mockito mock with a lambda to avoid
        // Temporal rejecting the @ActivityMethod annotation
        // inherited by the Mockito proxy class.
        worker.registerActivitiesImplementations(
                (HelloActivity) mockedActivity::greet);

        testEnv.start();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void sayHello_callsActivityWithCorrectArgument() {
        HelloWorkflow workflow = client.newWorkflowStub(
                HelloWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(HelloWorkflow.TASK_QUEUE)
                        .build());

        String result = workflow.sayHello("Temporal");

        assertEquals("Mocked: Hello, Temporal!", result);
        verify(mockedActivity).greet("Temporal");
    }
}
