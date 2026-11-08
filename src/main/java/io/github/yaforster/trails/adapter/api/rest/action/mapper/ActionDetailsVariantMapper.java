package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.core.test.Action;

public interface ActionDetailsVariantMapper {

    Class<? extends Action> actionType();

    Class<? extends ActionDetailsDTO> detailsType();

    default boolean supports(Action action) {
        return actionType().isInstance(action);
    }

    default boolean supports(ActionDetailsDTO details) {
        return detailsType().isInstance(details);
    }

    ActionDetailsDTO toDTO(Action action, ActionDetailsMappingContext context);

    Action fromDTO(ActionDefinitionDTO dto, ActionDetailsDTO details, ActionDetailsMappingContext context);

}
