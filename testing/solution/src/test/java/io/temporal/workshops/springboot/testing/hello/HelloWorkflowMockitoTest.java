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

    @Autowired
    private WorkflowClient workflowClient;

    @MockitoBean
    private HelloActivityImpl mockedActivity;

    @BeforeEach
    void setUp() {
        when(mockedActivity.greet(anyString()))
                .thenAnswer(inv -> "Mocked: Hello, "
                        + inv.getArgument(0) + "!");
    }

    @Test
    void sayHello_callsActivityWithCorrectArgument() {
        var workflow = workflowClient.newWorkflowStub(
                HelloWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(HelloWorkflow.TASK_QUEUE)
                        .build());

        var result = workflow.sayHello("Temporal");

        assertEquals("Mocked: Hello, Temporal!", result);
        verify(mockedActivity).greet("Temporal");
    }
}
