package io.temporal.app.config;

import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.spring.boot.WorkerOptionsCustomizer;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import io.temporal.worker.WorkflowImplementationOptions;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalOptionsConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TemporalOptionsConfig.class);

    @Bean
    public WorkerOptionsCustomizer customWorkerOptions() {
        return new WorkerOptionsCustomizer() {
            @Nonnull
            @Override
            public WorkerOptions.Builder customize(
                    @Nonnull WorkerOptions.Builder optionsBuilder,
                    @Nonnull String workerName,
                    @Nonnull String taskQueue) {
                // Customize the name of the worker.
                optionsBuilder.setIdentity("HelloWorldAppInstance-" + ProcessHandle.current().pid());

                // Example: auto-tune worker to use 75% of available resources.
                /*
                optionsBuilder.setWorkerTuner(
                        ResourceBasedTuner.newBuilder()
                                .setControllerOptions(
                                        ResourceBasedControllerOptions
                                                .newBuilder(0.75, 0.75)
                                                .build())
                                .build());
                */
                return optionsBuilder;
            }
        };
    }



    @Bean
    public TemporalOptionsCustomizer<WorkflowServiceStubsOptions.Builder>
    customServiceStubsOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkflowServiceStubsOptions.Builder customize(
                    @Nonnull WorkflowServiceStubsOptions.Builder optionsBuilder) {
                return optionsBuilder;
            }
        };
    }

    @Bean
    public TemporalOptionsCustomizer<WorkflowClientOptions.Builder>
    customClientOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkflowClientOptions.Builder customize(
                    @Nonnull WorkflowClientOptions.Builder optionsBuilder) {
                /* Example: set a custom data converter
                optionsBuilder.setDataConverter(
                        new CodecDataConverter(
                                DefaultDataConverter.newDefaultInstance(),
                                Collections.singletonList(new CryptCodec()),
                                true));
                */
                return optionsBuilder;
            }
        };
    }

    @Bean
    public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder>
    customWorkerFactoryOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkerFactoryOptions.Builder customize(
                    @Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
                return optionsBuilder;
            }
        };
    }

    @Bean
    public TemporalOptionsCustomizer<WorkflowImplementationOptions.Builder>
    customWorkflowImplementationOptions() {
        return new TemporalOptionsCustomizer<>() {
            @Nonnull
            @Override
            public WorkflowImplementationOptions.Builder customize(
                    @Nonnull WorkflowImplementationOptions.Builder optionsBuilder) {
                return optionsBuilder;
            }
        };
    }
}
