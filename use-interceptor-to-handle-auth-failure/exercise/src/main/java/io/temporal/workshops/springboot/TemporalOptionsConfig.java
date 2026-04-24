package io.temporal.workshops.springboot;


import io.temporal.client.WorkflowClientOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.workshops.springboot.interceptor.localactivity.auth.MDCContextPropagator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class TemporalOptionsConfig {

    @Bean
    public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> customClientOptions() {
        return optionsBuilder -> {
            optionsBuilder
                    .setContextPropagators(
                            Collections.singletonList(
                                    new MDCContextPropagator()
                            ));
            return optionsBuilder;
        };
    }

}
