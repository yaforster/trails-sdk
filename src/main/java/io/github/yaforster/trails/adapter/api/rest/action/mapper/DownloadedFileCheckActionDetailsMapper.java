package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DownloadedFileCheckDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.download.CheckDownloadedFileAction;

class DownloadedFileCheckActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckDownloadedFileAction, DownloadedFileCheckDetailsDTO> {

    DownloadedFileCheckActionDetailsMapper() {
        super(CheckDownloadedFileAction.class, DownloadedFileCheckDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckDownloadedFileAction action, ActionDetailsMappingContext context) {
        DownloadedFileCheckDetailsDTO dto = new DownloadedFileCheckDetailsDTO();
        dto.setFileName(action.getFileName());
        dto.setDetailsType(ActionDetailsTypeDTO.DOWNLOADED_FILE_CHECK);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, DownloadedFileCheckDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckDownloadedFileAction.builder()
                                        .actionID(dto.getReferenceID())
                                        .label(dto.getLabel())
                                        .nextActions(dto.getNextActions())
                                        .fileName(details.getFileName())
                                        .position(context.position(dto))
                                        .build();
    }

}
