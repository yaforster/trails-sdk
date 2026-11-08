package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionDetailsMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionDetailsMappers;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the closed Action transport catalogue to the validated persistence composition.
 */
@Configuration(proxyBeanMethods = false)
public class ActionTransportConfiguration {

    @Bean
    PositionMapper actionPositionMapper() {
        return Mappers.getMapper(PositionMapper.class);
    }

    @Bean
    ValueComputationInstructionMapper valueComputationInstructionMapper() {
        return new ValueComputationInstructionMapper();
    }

    @Bean
    ActionDetailsMapper actionDetailsMapper(PositionMapper actionPositionMapper,
            ValueComputationInstructionMapper valueComputationInstructionMapper) {
        return new ActionDetailsMapper(actionPositionMapper, valueComputationInstructionMapper,
                ActionDetailsMappers.all());
    }

    @Bean
    ActionMapper actionMapper(PositionMapper actionPositionMapper, ActionDetailsMapper actionDetailsMapper) {
        return new ActionMapper(actionPositionMapper, actionDetailsMapper);
    }

    @Bean
    ActionRequestValidator actionRequestValidator() {
        return new ActionRequestValidator();
    }

    @Bean
    ActionResponseAssembler actionResponseAssembler(ActionMapper actionMapper) {
        return new ActionResponseAssembler(actionMapper);
    }

    @Bean
    ActionElementReferenceResolver actionElementReferenceResolver(TrailsComposition composition) {
        return new ActionElementReferenceResolver(composition.persistence()
                                                             .ports()
                                                             .elementDatabaseService());
    }

    @Bean
    ActionController actionController(TrailsComposition composition, ActionRequestValidator actionRequestValidator,
            ActionResponseAssembler actionResponseAssembler, ActionElementReferenceResolver elementReferences) {
        return new ActionController(composition.persistence()
                                               .ports()
                                               .actionQueryService(),
                elementReferences, actionRequestValidator, actionResponseAssembler);
    }

}
