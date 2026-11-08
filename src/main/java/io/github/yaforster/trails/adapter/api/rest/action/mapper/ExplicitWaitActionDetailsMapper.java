package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ExplicitWaitDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.ExplicitWaitAction;

class ExplicitWaitActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<ExplicitWaitAction, ExplicitWaitDetailsDTO> {

    ExplicitWaitActionDetailsMapper() {
        super(ExplicitWaitAction.class, ExplicitWaitDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(ExplicitWaitAction action, ActionDetailsMappingContext context) {
        ExplicitWaitDetailsDTO dto = new ExplicitWaitDetailsDTO();
        dto.setDelayMillis(Math.max(0L, action.getDelayMillis()));
        dto.setDetailsType(ActionDetailsTypeDTO.EXPLICIT_WAIT);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, ExplicitWaitDetailsDTO details,
            ActionDetailsMappingContext context) {
        return ExplicitWaitAction.builder()
                                 .actionID(dto.getReferenceID())
                                 .label(dto.getLabel())
                                 .nextActions(dto.getNextActions())
                                 .delayMillis(context.nonNegativeOrZero(details.getDelayMillis()))
                                 .position(context.position(dto))
                                 .build();
    }

}
