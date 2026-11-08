package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.model.TestDataEntryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.TestDataEntry;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestDataControllerTest extends TrailsTest {

    @Test
    void shouldCreateTestDataWithItsContractLocationAndLinks() {
        TestDataDatabaseService testData = mock(TestDataDatabaseService.class);
        TestDataSet stored = new TestDataSet(17L, "Credentials", Instant.parse("2026-10-03T12:00:00Z"),
                List.of(new TestDataEntry("username", "alice")), false);
        when(testData.storeTestData(
                new TestDataSetDefinition("Credentials", List.of(new TestDataEntry("username", "alice"))))).thenReturn(
                        stored);
        TestDataController controller = controller(testData);

        ResponseEntity<TestDataSetDTO> response = controller.createNewTestData(
                new TestDataSetDefinitionDTO("Credentials", List.of(new TestDataEntryDTO().key("username")
                                                                                          .value("alice"))));

        assertThat(response).satisfies(result -> {
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getHeaders()
                             .getLocation()).hasToString("/api/test-data/17");
            assertThat(result.getBody()).satisfies(body -> {
                assertThat(body.getCreationDate()).hasToString("2026-10-03T12:00Z");
                assertThat(body.getLinks()).containsOnlyKeys("self", "update", "collection", "delete");
                assertThat(body.getLinks()
                               .get("update")
                               .getHref()).isEqualTo("/api/test-data/17");
                assertThat(body.getLinks()
                               .get("delete")
                               .getMethod()).isEqualTo("DELETE");
            });
        });
    }

    private TestDataController controller(TestDataDatabaseService testData) {
        TrailsAuthorization authorization = mock(TrailsAuthorization.class);
        when(authorization.canAccessTestData()).thenReturn(true);
        return new TestDataController(testData, authorization, new TestDataRequestValidator(),
                new TestDataResponseAssembler());
    }

}
