package io.temporal.workshops.springboot.testing.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorkflowTest {

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    @BeforeEach
    void setUp() {
        // TODO: Create a TestWorkflowEnvironment instance.
        // TODO: Get the WorkflowClient from the test environment.
        // TODO: Create a new Worker for HelloWorkflow.TASK_QUEUE.
        // TODO: Register HelloWorkflowImpl as a workflow implementation type.
        // TODO: Register a mocked HelloActivity using a lambda
        //       (e.g. name -> "Hello, " + name + "!").
        // TODO: Start the test environment.
    }

    @AfterEach
    void tearDown() {
        // TODO: Close the test environment.
    }

    @Test
    void sayHello_returnsGreeting() {
        // TODO: Create a workflow stub for HelloWorkflow
        //       using WorkflowOptions with the correct task queue.
        // TODO: Execute the workflow by calling sayHello("Temporal").
        // TODO: Assert the result equals the expected greeting.
    }
}
