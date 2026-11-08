package io.github.yaforster.trails.sdk.host.time;

import io.github.yaforster.trails.app.services.TimeSource;

/**
 * The host clock used for test-run timestamps.
 */
public final class SystemTimeSource implements TimeSource {

    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

}
