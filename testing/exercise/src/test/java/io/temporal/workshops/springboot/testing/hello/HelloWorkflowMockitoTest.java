package io.temporal.workshops.springboot.testing.hello;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class HelloWorkflowMockitoTest {

    // TODO: Inject the WorkflowClient using @Autowired.

    // TODO: Declare a @MockitoBean for
    //       HelloActivityImpl to replace the real
    //       activity bean with a Mockito mock.

    @BeforeEach
    void setUp() {
        // TODO: Configure the mock: when greet() is
        //       called with any string, return
        //       "Mocked: Hello, <name>!".
    }

    @Test
    void sayHello_callsActivityWithCorrectArgument() {
        // TODO: Create a workflow stub for
        //       HelloWorkflow.
        // TODO: Execute the workflow by calling
        //       sayHello("Temporal").
        // TODO: Assert the result equals the mocked
        //       response.
        // TODO: Verify that greet("Temporal") was
        //       called on the mock exactly once.
    }
}
