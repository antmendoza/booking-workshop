package io.temporal.app.domain.integrations;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.app.domain.messages.Name;

@ActivityInterface
public interface GreetingActivity {

    @ActivityMethod
    String greet1(Name name);

    @ActivityMethod
    String greet2(Name name);

    @ActivityMethod
    String greet3(Name name);

    @ActivityMethod
    void compensate1(Name name);

    @ActivityMethod
    void compensate2(Name name);

    @ActivityMethod
    void compensate3(Name name);
}
