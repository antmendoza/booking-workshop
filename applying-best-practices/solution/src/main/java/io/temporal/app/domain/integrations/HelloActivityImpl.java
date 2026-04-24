package io.temporal.app.domain.integrations;

import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import io.temporal.app.domain.messages.Name;

@Component
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloActivityImpl implements HelloActivity {

    private static final Logger log = LoggerFactory.getLogger(HelloActivityImpl.class);

    @Override
    public String greet(Name name) {
        log.info("Greeting: {}", name);
        return "Hello, " + name.getName() + "!";
    }
}
