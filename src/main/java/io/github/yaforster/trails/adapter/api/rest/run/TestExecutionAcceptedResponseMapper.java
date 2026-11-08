package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionStatusDTO;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Creates the contract-defined accepted response for a newly admitted test execution. */
public final class TestExecutionAcceptedResponseMapper {

    private final TestExecutionLinkFactory links;

    public TestExecutionAcceptedResponseMapper(TestExecutionLinkFactory links) {
        this.links = Objects.requireNonNull(links, "Test execution links are required");
    }

    public TestExecutionAcceptedDTO acceptedResponse(UUID executionId) {
        return new TestExecutionAcceptedDTO().executionId(executionId)
                                             .status(TestExecutionStatusDTO.ACCEPTED)
                                             .message("Test execution accepted")
                                             .links(Map.of("events", new LinkDTO().href(links.eventUri(executionId)
                                                                                             .toString())));
    }

}
