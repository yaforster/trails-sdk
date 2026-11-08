package io.github.yaforster.trails.sdk.notification;

import io.github.yaforster.trails.sdk.composition.AdapterId;

/** Receives one persisted terminal test-run notification on a host-managed executor. */
public interface Notifier {

    AdapterId adapterId();

    void notify(TerminalRunNotification notification);

}
