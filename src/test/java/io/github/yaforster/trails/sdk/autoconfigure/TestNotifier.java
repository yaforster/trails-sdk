package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.notification.Notifier;
import io.github.yaforster.trails.sdk.notification.TerminalRunNotification;

record TestNotifier(AdapterId adapterId) implements Notifier {

    TestNotifier(String adapterId) {
        this(new AdapterId(adapterId));
    }

    @Override
    public void notify(TerminalRunNotification notification) {}

}
