package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionDetailsMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PositionDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Position;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ActionMapperTest {

    private final PositionMapper positionMapper = mock(PositionMapper.class);
    private final ActionDetailsMapper actionDetailsMapper = mock(ActionDetailsMapper.class);
    private final ActionMapper mapper = new ActionMapper(positionMapper, actionDetailsMapper);

    @Test
    void toDTO_ShouldMapCommonFieldsAndDelegateDetails() {
        Action action = mock(Action.class);
        Position position = new Position(BigDecimal.ONE, BigDecimal.TEN);
        PositionDTO positionDTO = new PositionDTO();
        ClickDetailsDTO details = new ClickDetailsDTO();
        when(action.getActionID()).thenReturn(1L);
        when(action.getLabel()).thenReturn("label");
        when(action.getNextActions()).thenReturn(List.of(2L, 3L));
        when(action.getPosition()).thenReturn(position);
        when(positionMapper.toDTO(position)).thenReturn(positionDTO);
        ActionElementReferences references = new ActionElementReferences(Map.of(), Map.of());
        when(actionDetailsMapper.toDTO(action, references)).thenReturn(details);

        ActionDTO dto = mapper.toDTO(action, references);

        assertEquals(1L, dto.getActionID());
        assertEquals("label", dto.getLabel());
        assertEquals(List.of(2L, 3L), dto.getNextActions());
        assertSame(positionDTO, dto.getPosition());
        assertSame(details, dto.getDetails());
    }

    @Test
    void fromDefinitionDTO_ShouldDelegateDetailsMapping() {
        ActionDefinitionDTO dto = new ActionDefinitionDTO();
        Action action = mock(Action.class);
        ActionElementReferences references = new ActionElementReferences(Map.of(), Map.of());
        when(actionDetailsMapper.fromDTO(dto, references)).thenReturn(action);

        Action result = mapper.fromDefinitionDTO(dto, references);

        assertSame(action, result);
    }

}
