package io.temporal.workflow.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface HelloActivity {

    @ActivityMethod
    String greet(String name);
}
