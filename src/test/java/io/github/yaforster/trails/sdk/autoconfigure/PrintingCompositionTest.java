package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsPrintingRegistration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PrintingCompositionTest {

    @Test
    void shouldRejectStartup_whenNoPrintingProviderIsPresent() {
        composition().withPropertyValues("test.printer=false")
                     .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                             "Invalid Trails composition: exactly one printing registration is required, found 0"));
    }

    @Test
    void shouldRejectStartup_whenMultiplePrintingProvidersArePresent() {
        composition().withBean("otherPrinter", TrailsPrintingRegistration.class,
                () -> new TrailsPrintingRegistration(new AdapterId("other-printer"), mock()),
                definition -> definition.setPrimary(true))
                     .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                             "Invalid Trails composition: exactly one printing registration is required, found 2"));
    }

    @Test
    void shouldRetainTheRegisteredPrinter_whenCompositionIsValidated() {
        composition().run(context -> assertThat(context.getBean(TrailsComposition.class)
                                                       .printing()).isSameAs(
                                                               context.getBean(TrailsPrintingRegistration.class)));
    }

    @Test
    void shouldRejectAnUnregisteredPrintService_whenPrintingRegistrationIsMissing() {
        composition().withPropertyValues("test.printer=false")
                     .withBean(PrintService.class, () -> mock(PrintService.class))
                     .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                             "Invalid Trails composition: exactly one printing registration is required, found 0"));
    }

    private ApplicationContextRunner composition() {
        return new ApplicationContextRunner().withUserConfiguration(
                TrailsSdkAutoConfigurationTest.BootApplication.class,
                TrailsSdkAutoConfigurationTest.CompleteComposition.class);
    }
}
