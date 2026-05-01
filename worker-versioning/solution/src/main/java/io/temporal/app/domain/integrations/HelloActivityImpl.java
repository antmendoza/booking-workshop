package io.temporal.app.domain.integrations;

import io.temporal.app.domain.messages.Name;
import io.temporal.spring.boot.ActivityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ActivityImpl(taskQueues = "HelloSampleTaskQueue")
public class HelloActivityImpl implements HelloActivity {

    private static final Logger log =
            LoggerFactory.getLogger(HelloActivityImpl.class);

    private final String buildId;

    HelloActivityImpl(
            @Value("${spring.temporal.workers[0].deployment-properties.deployment-version:dev}") String buildId) {
        this.buildId = buildId;
    }

    @Override
    public String greet(Name name) {
        log.info("Greeting: {} (build={})", name, buildId);
        return "Hello, " + name.getName() + "! (served by " + buildId + ")";
    }

    @Override
    public String getWorkerVersion()
    {
        return buildId;
    }
}
