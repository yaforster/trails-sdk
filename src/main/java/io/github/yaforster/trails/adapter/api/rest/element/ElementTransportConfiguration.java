package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.core.CapabilityLabel;
import io.github.yaforster.trails.core.data.ImageType;
import io.github.yaforster.trails.core.data.ImageTypeDetector;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Wires the element resource exclusively to the validated Trails composition.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ElementScreenshotProperties.class)
public class ElementTransportConfiguration {

    @Bean
    TrailsScreenshotFileBuilder trailsScreenshotFileBuilder(ElementScreenshotProperties properties) {
        return new TrailsScreenshotFileBuilder(properties.maxSize()
                                                         .toBytes(),
                properties.maxWidth(), properties.maxHeight());
    }

    @Bean
    CapabilityContributor elementScreenshotCapabilityContributor(ElementScreenshotProperties properties) {
        String formats = ImageTypeDetector.supportedImageTypes()
                                          .stream()
                                          .map(ImageType::name)
                                          .collect(Collectors.joining(","));
        return () -> List.of(
                new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_HEIGHT,
                        String.valueOf(properties.maxHeight())),
                new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_WIDTH, String.valueOf(properties.maxWidth())),
                new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES,
                        String.valueOf(properties.maxSize()
                                                 .toBytes())),
                new CapabilityContribution(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, formats));
    }

    @Bean
    ElementRequestValidator elementRequestValidator(TrailsComposition composition) {
        return new ElementRequestValidator(composition.persistence()
                                                      .ports()
                                                      .elementDatabaseService());
    }

    @Bean
    ScreenshotFileRequestMapper screenshotFileRequestMapper(TrailsScreenshotFileBuilder files) {
        return new ScreenshotFileRequestMapper(files);
    }

    @Bean
    ElementResponseAssembler elementResponseAssembler() {
        return new ElementResponseAssembler();
    }

    @Bean
    ElementController elementController(TrailsComposition composition, TrailsAuthorization authorization,
            ElementRequestValidator requests, ScreenshotFileRequestMapper uploads, ElementResponseAssembler responses) {
        return new ElementController(composition.persistence()
                                                .ports()
                                                .elementDatabaseService(),
                composition.persistence()
                           .ports()
                           .screenshotDatabaseService(),
                composition.persistence()
                           .ports()
                           .stageDatabaseService(),
                authorization, requests, uploads, responses);
    }

}
