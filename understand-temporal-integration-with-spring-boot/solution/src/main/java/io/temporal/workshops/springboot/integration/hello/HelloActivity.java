package io.temporal.workshops.springboot.integration.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

// @ActivityInterface marks this as a Temporal activity contract.
// Like workflow interfaces, activity interfaces must be public so that workflow code can
// reference them when creating activity stubs.
@ActivityInterface
public interface HelloActivity {

    // @ActivityMethod marks the method as an activity entry point.
    // Activities contain the actual side-effecting business logic (API calls, DB writes, etc.)
    // that workflows orchestrate.
    @ActivityMethod
    String greet(String name);
}
