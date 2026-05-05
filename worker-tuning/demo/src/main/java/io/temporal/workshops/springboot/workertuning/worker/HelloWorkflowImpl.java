package io.temporal.workshops.springboot.workertuning.worker;

import io.temporal.activity.ActivityOptions;
import io.temporal.spring.boot.WorkflowImpl;
import io.temporal.workflow.Async;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@WorkflowImpl(taskQueues = HelloWorkflow.TASK_QUEUE)
@Profile("!starter")
public class HelloWorkflowImpl implements HelloWorkflow {

    private static final Logger LOGGER =
            Workflow.getLogger(HelloWorkflowImpl.class);

    private final HelloActivity helloActivity =
            Workflow.newActivityStub(
                    HelloActivity.class,
                    ActivityOptions.newBuilder()
                            .setStartToCloseTimeout(
                                    Duration.ofSeconds(10))
                            .build());

    @Override
    public String sayHello(String name) {


        while (true) {
            List<Promise<String>> activities = new ArrayList<>();
            for (int b = 0; b < 5; b++) {
                activities.add(Async.function(helloActivity::greet, "Hello " + name + b));
            }

            //wait for activities to complete
            Promise.allOf(activities).get();


            if (Workflow.getInfo().isContinueAsNewSuggested()) {
                Workflow.continueAsNew(name);
            }


        }


    }
}
