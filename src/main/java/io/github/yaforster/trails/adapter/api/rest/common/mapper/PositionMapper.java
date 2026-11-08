package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.PositionDTO;
import io.github.yaforster.trails.app.GlobalMapperConfig;
import io.github.yaforster.trails.core.test.Position;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface PositionMapper {

    @Mapping(source = "xCoordinatePixels", target = "xCoordinate")
    @Mapping(source = "yCoordinatePixels", target = "yCoordinate")
    PositionDTO toDTO(Position position);

    @Mapping(source = "xCoordinate", target = "xCoordinatePixels")
    @Mapping(source = "yCoordinate", target = "yCoordinatePixels")
    Position fromDTO(PositionDTO dto);

}
