package io.temporal.app.domain.integrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.app.domain.messages.Name;

@ActivityInterface
public interface HelloActivity {

    @ActivityMethod
    String greet(Name name);

    @ActivityMethod
    String getWorkerVersion();
}
