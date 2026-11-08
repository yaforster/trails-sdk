package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.ActionElementReferenceResolver;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ValueComputationInstructionMapper;
import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckCookieAction;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckLocalStorageAction;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckSessionStorageAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.MoveViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.document.CheckDownloadedDocumentTextAction;
import io.github.yaforster.trails.core.test.action.browser.*;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.download.*;
import io.github.yaforster.trails.core.test.action.element.*;
import io.github.yaforster.trails.core.test.action.value.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class ActionDetailsMapperTest {

    private final ElementDatabaseService elementDatabaseService = Mockito.mock(ElementDatabaseService.class);
    private final PositionMapper positionMapper = Mockito.mock(PositionMapper.class);
    private final ValueComputationInstructionMapper instructionMapper = Mockito.mock(
            ValueComputationInstructionMapper.class);
    private final TestableActionDetailsMapper detailsMapper = new TestableActionDetailsMapper(elementDatabaseService,
            positionMapper, instructionMapper, ActionDetailsMappers.all());

    private static final class TestableActionDetailsMapper extends ActionDetailsMapper {
        private final ElementDatabaseService elements;

        private TestableActionDetailsMapper(ElementDatabaseService elements, PositionMapper positions,
                ValueComputationInstructionMapper instructions, List<ActionDetailsVariantMapper> variants) {
            super(positions, instructions, variants);
            this.elements = elements;
        }

        ActionDetailsDTO toDTO(Action action, Long applicationId, Long stageId) {
            if (action instanceof WebpageAction webpageAction) {
                String locator = webpageAction.getLocatorForElementToActOn()
                                              .locatorString();
                Long id = elements.getElementIDByLocator(
                        new ElementDatabaseService.ElementLocator(applicationId, stageId, locator));
                return super.toDTO(action, new ActionElementReferences(Map.of(locator, id), Map.of()));
            }
            return super.toDTO(action, new ActionElementReferences(Map.of(), Map.of()));
        }

        Action fromDTO(ActionDefinitionDTO dto) {
            ActionElementReferences references = new ActionElementReferenceResolver(elements).forDefinition(
                    List.of(dto));
            return super.fromDTO(dto, references);
        }
    }

    private static Locator locator() {
        return new Locator(LocatorType.CSS, "#locator");
    }

    private static PositionDTO positionDTO() {
        PositionDTO dto = new PositionDTO();
        dto.setxCoordinate(BigDecimal.ONE);
        dto.setyCoordinate(BigDecimal.TEN);
        return dto;
    }

    private static FixedValueDTO fixedValueDTO(String value) {
        return new FixedValueDTO(ValueComputationTypeDTO.FIXED, value);
    }

    private static ActionDefinitionDTO definitionDTO(ActionDetailsDTO details, PositionDTO positionDTO) {
        ActionDefinitionDTO dto = new ActionDefinitionDTO();
        dto.setReferenceID(99L);
        dto.setLabel("label");
        dto.setNextActions(List.of(1L, 2L));
        dto.setPosition(positionDTO);
        dto.setDetails(details);
        return dto;
    }

    @Test
    void getDetails_ShouldMapCheckExistenceAction() {
        CheckExistenceAction action = Mockito.mock(CheckExistenceAction.class);
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(10L);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(CheckExistenceDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapClickAction() {
        ClickAction action = Mockito.mock(ClickAction.class);
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(20L);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(ClickDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapCoordinateClickActionWithoutElementLookup() {
        CoordinateClickAction action = CoordinateClickAction.builder()
                                                            .viewportCoordinates(new ViewportCoordinates(12, 34))
                                                            .build();

        CoordinateClickDetailsDTO details = assertInstanceOf(CoordinateClickDetailsDTO.class,
                detailsMapper.toDTO(action, 1L, 2L));

        assertEquals(ActionDetailsTypeDTO.COORDINATE_CLICK, details.getDetailsType());
        assertEquals(12, details.getxCoordinate());
        assertEquals(34, details.getyCoordinate());
        Mockito.verifyNoInteractions(elementDatabaseService);
    }

    @Test
    void getDetails_ShouldMapResizeViewportActionWithoutElementLookup() {
        ResizeViewportAction action = ResizeViewportAction.builder()
                                                          .viewportDimensions(new ViewportDimensions(800, 600))
                                                          .build();

        ResizeViewportDetailsDTO details = assertInstanceOf(ResizeViewportDetailsDTO.class,
                detailsMapper.toDTO(action, 1L, 2L));

        assertEquals(ActionDetailsTypeDTO.RESIZE_VIEWPORT, details.getDetailsType());
        assertEquals(800, details.getViewportWidth());
        assertEquals(600, details.getViewportHeight());
        Mockito.verifyNoInteractions(elementDatabaseService);
    }

    @Test
    void getDetails_ShouldResolveElementIdWithinApplicationStage_WhenScopeIsProvided() {
        ClickAction action = Mockito.mock(ClickAction.class);
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(21L);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        ClickDetailsDTO clickDetails = assertInstanceOf(ClickDetailsDTO.class, details);
        assertEquals(21L, clickDetails.getElementID());
    }

    @Test
    void getDetails_ShouldMapSelectionAction() {
        SelectAction action = Mockito.mock(SelectAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(30L);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(SelectionDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapSwitchWebsiteAction() {
        SwitchWebsiteAction action = Mockito.mock(SwitchWebsiteAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("https://a.b");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("https://a.b");
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(SwitchWebsiteDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapTypingAction() {
        TypingAction action = Mockito.mock(TypingAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(40L);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(TypingDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapTextCheckAction() {
        TextCheckAction action = Mockito.mock(TextCheckAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(50L);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(TextCheckDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapSessionStorageSearchAction() {
        CheckSessionStorageAction action = Mockito.mock(CheckSessionStorageAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getSessionStorageItemKey()).thenReturn("k1");
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(SessionStorageSearchDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapLocalStorageSearchAction() {
        CheckLocalStorageAction action = Mockito.mock(CheckLocalStorageAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getLocalStorageItemKey()).thenReturn("k2");
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(LocalStorageSearchDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapCookieSearchAction() {
        CheckCookieAction action = Mockito.mock(CheckCookieAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getCookieName()).thenReturn("cookie");
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(CookieSearchDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapExplicitWaitAction() {
        ExplicitWaitAction action = Mockito.mock(ExplicitWaitAction.class);
        when(action.getDelayMillis()).thenReturn(250L);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(ExplicitWaitDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapElementValueCheckAction() {
        ElementValueCheckAction action = Mockito.mock(ElementValueCheckAction.class);
        ValueComputationInstruction instruction = new FixedValueInstruction("abc");
        ValueComputationInstructionDTO instructionDTO = fixedValueDTO("abc");
        when(action.getLocatorForElementToActOn()).thenReturn(locator());
        when(action.getValueSource()).thenReturn(ElementValueSource.PROPERTY);
        when(action.getValueName()).thenReturn("checked");
        when(action.getComputationInstruction()).thenReturn(instruction);
        when(elementDatabaseService.getElementIDByLocator(
                new ElementDatabaseService.ElementLocator(1L, 2L, "#locator"))).thenReturn(60L);
        when(instructionMapper.toDTO(instruction)).thenReturn(instructionDTO);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        assertInstanceOf(ElementValueCheckDetailsDTO.class, details);
    }

    @Test
    void getDetails_ShouldMapDownloadedFileCheckAction() {
        CheckDownloadedFileAction action = Mockito.mock(CheckDownloadedFileAction.class);
        when(action.getFileName()).thenReturn("report.pdf");

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        DownloadedFileCheckDetailsDTO downloadedFileDetails = assertInstanceOf(DownloadedFileCheckDetailsDTO.class,
                details);
        assertEquals(ActionDetailsTypeDTO.DOWNLOADED_FILE_CHECK, downloadedFileDetails.getDetailsType());
        assertEquals("report.pdf", downloadedFileDetails.getFileName());
    }

    @Test
    void getDetails_ShouldMapDownloadedDocumentTextCheckAction() {
        CheckDownloadedDocumentTextAction action = Mockito.mock(CheckDownloadedDocumentTextAction.class);
        when(action.getFileName()).thenReturn("report.pdf");
        when(action.getExpectedText()).thenReturn("approved");
        when(action.isCaseSensitive()).thenReturn(true);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        DownloadedDocumentTextCheckDetailsDTO documentTextDetails = assertInstanceOf(
                DownloadedDocumentTextCheckDetailsDTO.class, details);
        assertEquals(ActionDetailsTypeDTO.DOWNLOADED_DOCUMENT_TEXT_CHECK, documentTextDetails.getDetailsType());
        assertEquals("report.pdf", documentTextDetails.getFileName());
        assertEquals("approved", documentTextDetails.getExpectedText());
        assertTrue(documentTextDetails.getCaseSensitive());
    }

    @Test
    void getDetails_ShouldMapMoveViewportAction() {
        MoveViewportAction action = Mockito.mock(MoveViewportAction.class);
        when(action.getMovement()).thenReturn(ViewportMove.SCROLL_BY);
        when(action.getDirection()).thenReturn(ViewportMoveDirection.DOWN);
        when(action.getAmount()).thenReturn(0.5D);
        when(action.getUnit()).thenReturn(ViewportMoveUnit.VIEWPORTS);
        when(action.getDelayAfterMoveMillis()).thenReturn(250L);

        ActionDetailsDTO details = detailsMapper.toDTO(action, 1L, 2L);

        ViewportMoveDetailsDTO moveDetails = assertInstanceOf(ViewportMoveDetailsDTO.class, details);
        assertEquals(ActionDetailsTypeDTO.VIEWPORT_MOVE, moveDetails.getDetailsType());
        assertEquals(ViewportMoveDTO.SCROLL_BY, moveDetails.getMovement());
        assertEquals(ViewportMoveDirectionDTO.DOWN, moveDetails.getDirection());
        assertEquals(0.5D, moveDetails.getAmount());
        assertEquals(ViewportMoveUnitDTO.VIEWPORTS, moveDetails.getUnit());
        assertEquals(250L, moveDetails.getDelayAfterMoveMillis());
    }

    @Test
    void getDetails_ShouldThrowForUnknownActionType() {
        Action unknownAction = Mockito.mock(Action.class);
        assertThrows(MappingException.class, () -> detailsMapper.toDTO(unknownAction, 1L, 2L));
    }

    @Test
    void fromDefinitionDTO_ShouldMapCheckExistenceAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        CheckExistenceDetailsDTO details = new CheckExistenceDetailsDTO();
        details.setElementID(101L);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        when(elementDatabaseService.getElementLocator(101L)).thenReturn(locator);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(CheckExistenceAction.class, result);
        CheckExistenceAction action = (CheckExistenceAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals(locator, action.getLocatorForElementToActOn());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldMapClickAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        ClickDetailsDTO details = new ClickDetailsDTO();
        details.setElementID(102L);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        when(elementDatabaseService.getElementLocator(102L)).thenReturn(locator);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(ClickAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapCoordinateClickActionWithoutElementLookup() {
        PositionDTO positionDTO = positionDTO();
        Position position = new Position(BigDecimal.ONE, BigDecimal.TEN);
        CoordinateClickDetailsDTO details = new CoordinateClickDetailsDTO(ActionDetailsTypeDTO.COORDINATE_CLICK, 12,
                34);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(position);

        CoordinateClickAction action = assertInstanceOf(CoordinateClickAction.class, detailsMapper.fromDTO(dto));

        assertEquals(new ViewportCoordinates(12, 34), action.getViewportCoordinates());
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals(position, action.getPosition());
        Mockito.verifyNoInteractions(elementDatabaseService);
    }

    @Test
    void fromDefinitionDTO_ShouldMapSelectionAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        SelectionDetailsDTO details = new SelectionDetailsDTO();
        details.setElementID(103L);
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(elementDatabaseService.getElementLocator(103L)).thenReturn(locator);
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(SelectAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapTypingAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        TypingDetailsDTO details = new TypingDetailsDTO();
        details.setElementID(104L);
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(elementDatabaseService.getElementLocator(104L)).thenReturn(locator);
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(TypingAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapSwitchWebsiteAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        SwitchWebsiteDetailsDTO details = new SwitchWebsiteDetailsDTO();
        details.setValueComputation(fixedValueDTO("u"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("u");
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(SwitchWebsiteAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapTextCheckAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        TextCheckDetailsDTO details = new TextCheckDetailsDTO();
        details.setElementID(105L);
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(elementDatabaseService.getElementLocator(105L)).thenReturn(locator);
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(TextCheckAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapSessionStorageSearchAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        SessionStorageSearchDetailsDTO details = new SessionStorageSearchDetailsDTO();
        details.setStorageKey("ss");
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(CheckSessionStorageAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapLocalStorageSearchAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        LocalStorageSearchDetailsDTO details = new LocalStorageSearchDetailsDTO();
        details.setStorageKey("ls");
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(CheckLocalStorageAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapCookieSearchAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        CookieSearchDetailsDTO details = new CookieSearchDetailsDTO();
        details.setCookieName("cookie");
        details.setValueComputation(fixedValueDTO("x"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("x");
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);
        assertInstanceOf(CheckCookieAction.class, result);
    }

    @Test
    void fromDefinitionDTO_ShouldMapExplicitWaitAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        ExplicitWaitDetailsDTO details = new ExplicitWaitDetailsDTO();
        details.setDelayMillis(250L);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(ExplicitWaitAction.class, result);
        ExplicitWaitAction action = (ExplicitWaitAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals(250L, action.getDelayMillis());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldMapElementValueCheckAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        ElementValueCheckDetailsDTO details = new ElementValueCheckDetailsDTO();
        details.setElementID(106L);
        details.setValueSource(ElementValueCheckSourceDTO.CSS_VALUE);
        details.setValueName("display");
        details.setValueComputation(fixedValueDTO("block"));
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        Locator locator = locator();
        ValueComputationInstruction mappedInstruction = new FixedValueInstruction("block");
        when(elementDatabaseService.getElementLocator(106L)).thenReturn(locator);
        when(instructionMapper.fromDTO(details.getValueComputation())).thenReturn(mappedInstruction);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(ElementValueCheckAction.class, result);
        ElementValueCheckAction action = (ElementValueCheckAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals(locator, action.getLocatorForElementToActOn());
        assertEquals(ElementValueSource.CSS_VALUE, action.getValueSource());
        assertEquals("display", action.getValueName());
        assertEquals(mappedInstruction, action.getComputationInstruction());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldMapDownloadedFileCheckAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        DownloadedFileCheckDetailsDTO details = new DownloadedFileCheckDetailsDTO();
        details.setFileName("report.pdf");
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(CheckDownloadedFileAction.class, result);
        CheckDownloadedFileAction action = (CheckDownloadedFileAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals("report.pdf", action.getFileName());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldMapDownloadedDocumentTextCheckAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        DownloadedDocumentTextCheckDetailsDTO details = new DownloadedDocumentTextCheckDetailsDTO();
        details.setFileName("report.pdf");
        details.setExpectedText("approved");
        details.setCaseSensitive(true);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(CheckDownloadedDocumentTextAction.class, result);
        CheckDownloadedDocumentTextAction action = (CheckDownloadedDocumentTextAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals("report.pdf", action.getFileName());
        assertEquals("approved", action.getExpectedText());
        assertTrue(action.isCaseSensitive());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldMapMoveViewportAction() {
        PositionDTO positionDTO = positionDTO();
        Position mappedPosition = new Position(BigDecimal.ONE, BigDecimal.TEN);
        ViewportMoveDetailsDTO details = new ViewportMoveDetailsDTO();
        details.setMovement(ViewportMoveDTO.PAGE_FLIP);
        details.setDirection(ViewportMoveDirectionDTO.UP);
        details.setAmount(2D);
        details.setUnit(ViewportMoveUnitDTO.VIEWPORTS);
        details.setDelayAfterMoveMillis(300L);
        ActionDefinitionDTO dto = definitionDTO(details, positionDTO);
        when(positionMapper.fromDTO(positionDTO)).thenReturn(mappedPosition);

        Action result = detailsMapper.fromDTO(dto);

        assertInstanceOf(MoveViewportAction.class, result);
        MoveViewportAction action = (MoveViewportAction) result;
        assertEquals(dto.getReferenceID(), action.getActionID());
        assertEquals(dto.getLabel(), action.getLabel());
        assertEquals(dto.getNextActions(), action.getNextActions());
        assertEquals(ViewportMove.PAGE_FLIP, action.getMovement());
        assertEquals(ViewportMoveDirection.UP, action.getDirection());
        assertEquals(2D, action.getAmount());
        assertEquals(ViewportMoveUnit.VIEWPORTS, action.getUnit());
        assertEquals(300L, action.getDelayAfterMoveMillis());
        assertEquals(mappedPosition, action.getPosition());
    }

    @Test
    void fromDefinitionDTO_ShouldThrowForUnknownDetailsType() {
        ActionDefinitionDTO dto = definitionDTO(Mockito.mock(ActionDetailsDTO.class), positionDTO());
        assertThrows(MappingException.class, () -> detailsMapper.fromDTO(dto));
    }

}
