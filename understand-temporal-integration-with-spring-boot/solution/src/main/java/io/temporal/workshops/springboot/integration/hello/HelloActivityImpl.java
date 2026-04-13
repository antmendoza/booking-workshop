package io.temporal.workshops.springboot.integration.hello;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloActivityImpl implements HelloActivity {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(String name) {
        LOGGER.info("Greeting: {}", name);
        return "Hello, " + name + "!";
    }
}
