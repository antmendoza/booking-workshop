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
        // TODO: Create a TestWorkflowEnvironment instance.
        // TODO: Get the WorkflowClient from the test environment.
        // TODO: Create a Mockito mock of HelloActivity.
        // TODO: Configure the mock: when greet() is called with
        //       any string, return "Mocked: Hello, <name>!".
        // TODO: Create a new Worker for HelloWorkflow.TASK_QUEUE.
        // TODO: Register HelloWorkflowImpl as a workflow type.
        // TODO: Register the activity using a lambda that
        //       delegates to the Mockito mock. A Mockito proxy
        //       inherits @ActivityMethod, which Temporal rejects
        //       on concrete classes. Wrapping with a lambda
        //       avoids this:
        //         (HelloActivity) mockedActivity::greet
        // TODO: Start the test environment.
    }

    @AfterEach
    void tearDown() {
        // TODO: Close the test environment.
    }

    @Test
    void sayHello_callsActivityWithCorrectArgument() {
        // TODO: Create a workflow stub for HelloWorkflow.
        // TODO: Execute the workflow by calling
        //       sayHello("Temporal").
        // TODO: Assert the result equals the mocked response.
        // TODO: Verify that greet("Temporal") was called on the
        //       mock exactly once.
    }
}
