package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsBrowserRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsCompositionValidator;
import io.github.yaforster.trails.sdk.composition.TrailsPersistenceRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsPrintingRegistration;
import io.github.yaforster.trails.sdk.notification.Notifier;
import io.github.yaforster.trails.sdk.notification.NotifierDispatcher;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Installs the fail-fast validation required before the Trails host becomes ready. */
@AutoConfiguration(before = ServletWebSecurityAutoConfiguration.class)
@Import({TrailsHostConfiguration.class, TrailsEndpointSecurityConfiguration.class, TrailsSecurityConfiguration.class,
        TrailsTransportConfiguration.class})
@EnableConfigurationProperties({NotifierDispatchProperties.class, TestExecutionProperties.class,
        ActionGraphProperties.class})
public class TrailsSdkAutoConfiguration {

    @Bean
    TrailsComposition trailsComposition(List<TrailsPersistenceRegistration> persistenceRegistrations,
            List<TrailsBrowserRegistration> browserRegistrations,
            List<TrailsPrintingRegistration> printingRegistrations,
            List<TrailsAuthenticationRegistration> authenticationRegistrations, List<Notifier> notifiers) {
        return TrailsCompositionValidator.validate(persistenceRegistrations, browserRegistrations,
                printingRegistrations, authenticationRegistrations, notifiers);
    }

    @Bean(destroyMethod = "close")
    NotifierDispatcher notifierDispatcher(TrailsComposition composition, NotifierDispatchProperties properties) {
        return new NotifierDispatcher(composition.notifiers(), properties.limits());
    }

}
