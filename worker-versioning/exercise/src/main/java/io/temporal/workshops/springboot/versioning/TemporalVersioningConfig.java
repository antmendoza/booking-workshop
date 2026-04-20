package io.temporal.workshops.springboot.versioning;

import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkerOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TemporalVersioningConfig.VersioningProperties.class)
class TemporalVersioningConfig {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TemporalVersioningConfig.class);

    @Bean
    TemporalOptionsCustomizer<WorkerOptions.Builder> workerVersioningCustomizer(
            VersioningProperties properties) {
        // TODO: If properties.enabled() is false, return a pass-through customizer
        //       (optionsBuilder -> optionsBuilder) and log "Worker versioning disabled".
        // TODO: Otherwise, return a customizer that calls
        //         optionsBuilder.setDeploymentOptions(
        //             WorkerDeploymentOptions.newBuilder()
        //                 .setVersion(new WorkerDeploymentVersion(
        //                     properties.deploymentName(), properties.buildId()))
        //                 .setUseVersioning(true)
        //                 .build())
        //       and log "Worker versioning enabled: deployment={}, buildId={}".
        return optionsBuilder -> optionsBuilder;
    }

    @ConfigurationProperties(prefix = "app.versioning")
    record VersioningProperties(
            boolean enabled,
            String deploymentName,
            String buildId
    ) {
        VersioningProperties {
            if (deploymentName == null || deploymentName.isBlank()) {
                deploymentName = "booking-workers";
            }
            if (buildId == null || buildId.isBlank()) {
                buildId = "1.0";
            }
        }
    }
}
