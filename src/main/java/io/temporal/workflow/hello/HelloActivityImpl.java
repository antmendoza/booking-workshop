package io.temporal.workflow.hello;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
@Profile(value = "hello")
public class HelloActivityImpl implements HelloActivity {

    private static final Logger log = LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(String name) {
        log.info("Greeting: {}", name);
        return "Hello, " + name + "!";
    }
}
