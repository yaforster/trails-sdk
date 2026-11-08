package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ValueComputationInstructionDTO;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

public record ActionDetailsMappingContext(ActionElementReferences elements, PositionMapper positionMapper,
        ValueComputationInstructionMapper instructionMapper) {

    Long elementId(Locator locator) {
        return elements.elementId(locator);
    }

    Locator elementLocator(Long elementId) {
        return elements.elementLocator(elementId);
    }

    Position position(ActionDefinitionDTO dto) {
        return positionMapper.fromDTO(dto.getPosition());
    }

    ValueComputationInstructionDTO toDTO(ValueComputationInstruction instruction) {
        return instructionMapper.toDTO(instruction);
    }

    ValueComputationInstruction fromDTO(ValueComputationInstructionDTO dto) {
        return instructionMapper.fromDTO(dto);
    }

    long nonNegativeOrZero(Long value) {
        if (value == null) {
            return 0L;
        }
        return Math.max(0L, value);
    }
}
