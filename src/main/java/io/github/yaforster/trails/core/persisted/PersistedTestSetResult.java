package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.test.Browser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record PersistedTestSetResult(Long id, Long testRunId, Long applicationId, Long stageId, Integer totalRunTime,
        Browser browserToRunIn, String testPlanLabel, OffsetDateTime testCaseTimestamp, Browser testedInBrowser) {

    public static final OffsetDateTime NO_TEST_CASE_TIMESTAMP = Instant.EPOCH.atOffset(ZoneOffset.UTC);

}
