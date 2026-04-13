package io.temporal.workshops.springboot.integration.hello;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// @Component makes this a Spring-managed bean, enabling constructor injection of any Spring
// dependency (services, repositories, config properties, etc.). This is the key advantage
// of the Spring Boot integration: activities can use the full Spring ecosystem.
//
// @ActivityImpl is the activity counterpart of @WorkflowImpl — it auto-registers this
// implementation with a worker on the specified task queue.
//
// Note: workflow implementations must NOT be Spring beans (@Component) because Temporal
// creates a new instance per workflow execution. Only activity implementations get @Component.
@Component
@ActivityImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
class HelloActivityImpl implements HelloActivity {

    // Activities run as normal Java code (no replay), so a standard SLF4J logger is fine here.
    // Compare with HelloWorkflowImpl, which must use Workflow.getLogger().
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(String name) {
        LOGGER.info("Greeting: {}", name);
        return "Hello, " + name + "!";
    }
}
