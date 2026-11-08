package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CookieSearchDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckCookieAction;

class CookieSearchActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckCookieAction, CookieSearchDetailsDTO> {

    CookieSearchActionDetailsMapper() {
        super(CheckCookieAction.class, CookieSearchDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckCookieAction action, ActionDetailsMappingContext context) {
        CookieSearchDetailsDTO dto = new CookieSearchDetailsDTO();
        dto.setCookieName(action.getCookieName());
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.COOKIE_SEARCH);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, CookieSearchDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckCookieAction.builder()
                                .actionID(dto.getReferenceID())
                                .label(dto.getLabel())
                                .nextActions(dto.getNextActions())
                                .cookieName(details.getCookieName())
                                .computationInstruction(context.fromDTO(details.getValueComputation()))
                                .position(context.position(dto))
                                .build();
    }

}
