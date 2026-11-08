package io.github.yaforster.trails.adapter.api.rest.capabilities;

import io.github.yaforster.trails.app.services.CapabilitiesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the capability metadata resource to the SDK-owned aggregation service. */
@Configuration(proxyBeanMethods = false)
public class CapabilitiesTransportConfiguration {

    @Bean
    CapabilitiesController capabilitiesController(CapabilitiesService capabilities) {
        return new CapabilitiesController(capabilities);
    }

}
