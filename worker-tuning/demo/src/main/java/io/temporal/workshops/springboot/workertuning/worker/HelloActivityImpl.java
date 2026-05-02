package io.temporal.workshops.springboot.workertuning.worker;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
@Profile("!starter")
class HelloActivityImpl implements HelloActivity {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(String name) {
        LOGGER.info("Greeting: {}", name);
        try {
            Thread.sleep((long) (Math.random()*100 + 3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "Hello, " + name + "!";
    }
}
