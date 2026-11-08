package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DownloadedDocumentTextCheckDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.document.CheckDownloadedDocumentTextAction;

class DownloadedDocumentTextCheckActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckDownloadedDocumentTextAction, DownloadedDocumentTextCheckDetailsDTO> {

    DownloadedDocumentTextCheckActionDetailsMapper() {
        super(CheckDownloadedDocumentTextAction.class, DownloadedDocumentTextCheckDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckDownloadedDocumentTextAction action,
            ActionDetailsMappingContext context) {
        DownloadedDocumentTextCheckDetailsDTO dto = new DownloadedDocumentTextCheckDetailsDTO();
        dto.setDetailsType(ActionDetailsTypeDTO.DOWNLOADED_DOCUMENT_TEXT_CHECK);
        dto.setFileName(action.getFileName());
        dto.setExpectedText(action.getExpectedText());
        dto.setCaseSensitive(action.isCaseSensitive());
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, DownloadedDocumentTextCheckDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckDownloadedDocumentTextAction.builder()
                                                .actionID(dto.getReferenceID())
                                                .label(dto.getLabel())
                                                .nextActions(dto.getNextActions())
                                                .fileName(details.getFileName())
                                                .expectedText(details.getExpectedText())
                                                .caseSensitive(Boolean.TRUE.equals(details.getCaseSensitive()))
                                                .position(context.position(dto))
                                                .build();
    }

}
