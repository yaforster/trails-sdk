package io.github.yaforster.trails.sdk.notification;

/** The synchronous admission outcome for one terminal notification fan-out. */
public record NotifierDispatchResult(int accepted, int rejected) {

    public NotifierDispatchResult {
        if (accepted < 0 || rejected < 0) {
            throw new IllegalArgumentException("Notifier dispatch counts cannot be negative");
        }
    }

}
