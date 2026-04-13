package io.temporal.workshops.springboot.testing.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class HelloWorkflowTest {

    // TODO: Inject the WorkflowClient using @Autowired.

    @Test
    void sayHello_returnsGreeting() {
        // TODO: Create a workflow stub for HelloWorkflow
        //       using WorkflowOptions with the correct
        //       task queue.
        // TODO: Execute the workflow by calling
        //       sayHello("Temporal").
        // TODO: Assert the result equals
        //       "Hello, Temporal!".
    }
}
