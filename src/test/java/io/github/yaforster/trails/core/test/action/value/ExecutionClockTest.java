package io.github.yaforster.trails.core.test.action.value;

import io.github.yaforster.trails.core.test.BrowserExecution;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionClockTest {
    @Test
    void shouldUseExecutionZone_whenComputingRelativeDate() {
        Clock tokyoClock = Clock.fixed(Instant.parse("2026-11-05T23:30:00Z"), ZoneId.of("Asia/Tokyo"));
        TestExecutionContext context = context(tokyoClock);

        String value = context.compute(new RelativeDateInstruction(1, "uuuu-MM-dd"));

        assertThat(value).isEqualTo("2026-11-07");
    }

    @Test
    void shouldUseExecutionInstant_whenFormattingTimestamp() {
        Clock tokyoClock = Clock.fixed(Instant.parse("2026-11-05T23:30:00Z"), ZoneId.of("Asia/Tokyo"));
        TestExecutionContext context = context(tokyoClock);

        String value = context.compute(new TimeStampInstruction("uuuu-MM-dd HH:mm"));

        assertThat(value).isEqualTo("2026-11-06 08:30");
    }

    private TestExecutionContext context(Clock clock) {
        BrowserExecution execution = new BrowserExecution(new TestExecutionContexts.FakeBrowserSession(),
                new TestExecutionContexts.FakeDocumentExecution());
        return new TestExecutionContext(execution, clock);
    }
}
