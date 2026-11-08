package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResizeViewportDetailsDTO;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ActionDetailsRawJacksonCustomizer {

    @Bean
    JsonMapperBuilderCustomizer actionDetailsRawCustomizer() {
        return builder -> {
            builder.addMixIn(CoordinateClickDetailsDTO.class, CoordinateClickDetailsDTOMixin.class);
            builder.addMixIn(ResizeViewportDetailsDTO.class, ResizeViewportDetailsDTOMixin.class);
        };
    }

}
