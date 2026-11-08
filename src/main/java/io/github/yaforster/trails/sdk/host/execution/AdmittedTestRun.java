package io.github.yaforster.trails.sdk.host.execution;

import java.util.concurrent.atomic.AtomicBoolean;

/** Gives an admitted run exactly one owner: execution or cancellation before start. */
final class AdmittedTestRun implements Runnable {
    private final AtomicBoolean claimed = new AtomicBoolean();
    private final Runnable execute;
    private final Runnable cancel;

    AdmittedTestRun(Runnable execute, Runnable cancel) {
        this.execute = execute;
        this.cancel = cancel;
    }

    @Override
    public void run() {
        if (claimed.compareAndSet(false, true)) {
            execute.run();
        }
    }

    void cancelBeforeStart() {
        if (claimed.compareAndSet(false, true)) {
            cancel.run();
        }
    }
}
