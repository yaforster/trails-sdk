package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.core.test.Action;

abstract class AbstractActionDetailsVariantMapper<A extends Action, D extends ActionDetailsDTO>
        implements
            ActionDetailsVariantMapper {

    private final Class<A> actionType;
    private final Class<D> detailsType;

    protected AbstractActionDetailsVariantMapper(Class<A> actionType, Class<D> detailsType) {
        this.actionType = actionType;
        this.detailsType = detailsType;
    }

    @Override
    public Class<? extends Action> actionType() {
        return actionType;
    }

    @Override
    public Class<? extends ActionDetailsDTO> detailsType() {
        return detailsType;
    }

    @Override
    public ActionDetailsDTO toDTO(Action action, ActionDetailsMappingContext context) {
        return toVariantDTO(actionType.cast(action), context);
    }

    @Override
    public Action fromDTO(ActionDefinitionDTO dto, ActionDetailsDTO details, ActionDetailsMappingContext context) {
        return fromVariantDTO(dto, detailsType.cast(details), context);
    }

    protected abstract ActionDetailsDTO toVariantDTO(A action, ActionDetailsMappingContext context);

    protected abstract Action fromVariantDTO(ActionDefinitionDTO dto, D details, ActionDetailsMappingContext context);

}
