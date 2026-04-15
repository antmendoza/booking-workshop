package io.temporal.workshops.springboot.integration.hello;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

// Spring bean auto-registered on HelloTaskQueue — can inject any Spring dependency
@Component
@ActivityImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
class HelloActivityImpl implements HelloActivity {

    // Standard logger — activities don't replay
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(String name) {
        LOGGER.info("Greeting: {}", name);
        return "Hello, " + name + "!";
    }
}
