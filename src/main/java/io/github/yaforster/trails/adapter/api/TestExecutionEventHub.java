package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.app.services.TimeSource;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashMap;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Delivers execution events to SSE subscribers and retains bounded terminal-event replay.
 */
public final class TestExecutionEventHub {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;
    private static final long TERMINAL_EVENT_REPLAY_WINDOW_MS = 10 * 60 * 1000L;
    private static final int MAXIMUM_TERMINAL_EVENT_COUNT = 100;
    private static final String EVENT_TEST_STARTED = "test.started";
    private static final String EVENT_TEST_COMPLETED = "test.completed";
    private static final String EVENT_TEST_FAILED = "test.failed";
    private static final String EVENT_TEST_TERMINAL = "test.terminal";
    private final Object lifecycle = new Object();
    private final Map<UUID, List<SseEmitter>> emittersByExecutionId = new HashMap<>();
    private final Map<UUID, TerminalEvent> terminalEventsByExecutionId = new LinkedHashMap<>();
    private final TimeSource timeSource;

    public TestExecutionEventHub(TimeSource timeSource) {
        this.timeSource = Objects.requireNonNull(timeSource, "Time source is required");
    }

    public SseEmitter subscribe(UUID executionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        subscribe(executionId, emitter);
        return emitter;
    }

    void subscribe(UUID executionId, SseEmitter emitter) {
        Objects.requireNonNull(executionId, "Execution identifier is required");
        registerLifecycleCallbacks(executionId, emitter);
        TerminalEvent terminal;
        synchronized (lifecycle) {
            removeExpiredTerminalEvents();
            terminal = terminalEventsByExecutionId.get(executionId);
            if (terminal == null) {
                emittersByExecutionId.computeIfAbsent(executionId, _ -> new ArrayList<>())
                                     .add(emitter);
            }
        }
        if (terminal != null) {
            trySend(emitter, EVENT_TEST_TERMINAL, terminal.event());
            completeQuietly(emitter);
        }
    }

    public void publishStarted(UUID executionId, Object event) {
        publish(executionId, event);
    }

    public void publishCompleted(UUID executionId, Object event) {
        publishTerminalEvent(executionId, EVENT_TEST_COMPLETED, event);
    }

    public void publishFailed(UUID executionId, Object event) {
        publishTerminalEvent(executionId, EVENT_TEST_FAILED, event);
    }

    void registerLifecycleCallbacks(UUID executionId, SseEmitter emitter) {
        emitter.onCompletion(() -> removeEmitter(executionId, emitter));
        emitter.onTimeout(() -> removeEmitter(executionId, emitter));
        emitter.onError(_ -> removeEmitter(executionId, emitter));
    }

    void publishTerminalEvent(UUID executionId, String eventName, Object event) {
        List<SseEmitter> subscribers;
        synchronized (lifecycle) {
            storeTerminalEvent(executionId, event);
            subscribers = emittersByExecutionId.remove(executionId);
        }
        if (subscribers != null) {
            for (SseEmitter emitter : subscribers) {
                trySend(emitter, eventName, event);
                completeQuietly(emitter);
            }
        }
    }

    Object terminalEvent(UUID executionId) {
        synchronized (lifecycle) {
            removeExpiredTerminalEvents();
            TerminalEvent terminalEvent = terminalEventsByExecutionId.get(executionId);
            if (terminalEvent == null) {
                return null;
            }
            return terminalEvent.event();
        }
    }

    int terminalEventCount() {
        synchronized (lifecycle) {
            removeExpiredTerminalEvents();
            return terminalEventsByExecutionId.size();
        }
    }

    private void storeTerminalEvent(UUID executionId, Object event) {
        Objects.requireNonNull(executionId, "Execution identifier is required");
        Objects.requireNonNull(event, "Execution event is required");
        synchronized (lifecycle) {
            removeExpiredTerminalEvents();
            terminalEventsByExecutionId.remove(executionId);
            terminalEventsByExecutionId.put(executionId, new TerminalEvent(event, timeSource.currentTimeMillis()));
            removeOldestTerminalEventsAboveCapacity();
        }
    }

    private void removeExpiredTerminalEvents() {
        long expiryTimestamp = timeSource.currentTimeMillis() - TERMINAL_EVENT_REPLAY_WINDOW_MS;
        terminalEventsByExecutionId.entrySet()
                                   .removeIf(entry -> entry.getValue()
                                                           .publishedAt() <= expiryTimestamp);
    }

    private void removeOldestTerminalEventsAboveCapacity() {
        Iterator<UUID> executionIds = terminalEventsByExecutionId.keySet()
                                                                 .iterator();
        while (terminalEventsByExecutionId.size() > MAXIMUM_TERMINAL_EVENT_COUNT && executionIds.hasNext()) {
            executionIds.next();
            executionIds.remove();
        }
    }

    private void publish(UUID executionId, Object event) {
        List<SseEmitter> subscribers;
        synchronized (lifecycle) {
            subscribers = List.copyOf(emittersByExecutionId.getOrDefault(executionId, List.of()));
        }
        for (SseEmitter emitter : subscribers) {
            if (!trySend(emitter, TestExecutionEventHub.EVENT_TEST_STARTED, event)) {
                removeEmitter(executionId, emitter);
            }
        }
    }

    private boolean trySend(SseEmitter emitter, String eventName, Object event) {
        try {
            emitter.send(SseEmitter.event()
                                   .name(eventName)
                                   .data(event));
            return true;
        } catch (IOException exception) {
            emitter.completeWithError(exception);
            return false;
        } catch (IllegalStateException exception) {
            return false;
        }
    }

    private void completeQuietly(SseEmitter emitter) {
        try {
            emitter.complete();
        } catch (IllegalStateException exception) {
            // The client disconnected before terminal event cleanup.
        }
    }

    private void removeEmitter(UUID executionId, SseEmitter emitter) {
        synchronized (lifecycle) {
            emittersByExecutionId.computeIfPresent(executionId, (_, emitters) -> {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    return null;
                }
                return emitters;
            });
        }
    }

}
