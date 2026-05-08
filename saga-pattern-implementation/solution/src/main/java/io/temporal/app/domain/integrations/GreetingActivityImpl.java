package io.temporal.app.domain.integrations;

import io.temporal.app.domain.messages.Name;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "SagaTaskQueue")
public class GreetingActivityImpl implements GreetingActivity {

    private static final Logger log = LoggerFactory.getLogger(GreetingActivityImpl.class);

    @Override
    public String greet1(Name name) {
        log.info("Greet1: greeting {}", name.getName());
        if ("Fail-1".equals(name.getFirstName())) {
            throw new RuntimeException("Greet1 simulated failure for " + name.getName());
        }
        return "Hello " + name.getFirstName() + " " + name.getLastName() + "-1";
    }

    @Override
    public String greet2(Name name) {
        log.info("Greet2: greeting {}", name.getName());
        if ("Fail-2".equals(name.getFirstName())) {
            throw new RuntimeException("Greet2 simulated failure for " + name.getName());
        }
        return "Hello " + name.getFirstName() + " " + name.getLastName() + "-2";
    }

    @Override
    public String greet3(Name name) {
        log.info("Greet3: greeting {}", name.getName());
        if ("Fail-3".equals(name.getFirstName())) {
            throw new RuntimeException("Greet3 simulated failure for " + name.getName());
        }
        return "Hello " + name.getFirstName() + " " + name.getLastName() + "-3";
    }

    @Override
    public void compensate1(Name name) {
        log.info("Greet1: compensating for {}", name.getName());
    }

    @Override
    public void compensate2(Name name) {
        log.info("Greet2: compensating for {}", name.getName());
    }

    @Override
    public void compensate3(Name name) {
        log.info("Greet3: compensating for {}", name.getName());
    }
}
