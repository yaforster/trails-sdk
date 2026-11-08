package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.core.data.TestDataSet;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDataResponseAssemblerTest {
    @Test
    void shouldOnlyOfferMutationLinksWithSuppliedAccess() {
        TestDataSet dataSet = new TestDataSet(42L, "data", Instant.EPOCH, List.of(), false);
        TestDataResponseAssembler assembler = new TestDataResponseAssembler();

        boolean denied = assembler.testData(dataSet, false, false)
                                  .getLinks()
                                  .containsKey("delete");
        boolean allowed = assembler.testData(dataSet, false, true)
                                   .getLinks()
                                   .containsKey("delete");

        assertThat(List.of(denied, allowed)).containsExactly(false, true);
    }

    @Test
    void shouldLinkToContractDeleteRoute_whenTestDataIsActive() {
        TestDataSet dataSet = new TestDataSet(42L, "data", Instant.EPOCH, List.of(), false);

        String deleteUri = new TestDataResponseAssembler().testData(dataSet, false, true)
                                                          .getLinks()
                                                          .get("delete")
                                                          .getHref();

        assertThat(deleteUri).isEqualTo("/api/test-data/42");
    }
}
