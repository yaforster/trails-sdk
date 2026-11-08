package io.github.yaforster.trails.core.test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validates before expansion; reverse topological counting never enumerates execution paths. */
public final class ValidatedActionGraph {
    private final List<Long> executionOrder;
    private final Map<Long, List<Long>> successors;

    private ValidatedActionGraph(List<Long> executionOrder, Map<Long, List<Long>> successors) {
        this.executionOrder = List.copyOf(executionOrder);
        Map<Long, List<Long>> snapshot = new LinkedHashMap<>();
        successors.forEach((id, next) -> snapshot.put(id, List.copyOf(next)));
        this.successors = Map.copyOf(snapshot);
    }

    public List<Long> executionOrder() {
        return executionOrder;
    }

    public Map<Long, List<Long>> successors() {
        return successors;
    }

    public static ValidatedActionGraph validate(Map<Long, List<Long>> successors, ActionGraphLimits limits) {
        requireActionCount(successors, limits);
        Map<Long, Integer> remainingPredecessorsByActionId = countIncomingEdges(successors);
        List<Long> topologicalOrder = orderActions(successors, remainingPredecessorsByActionId);
        requireBoundedPaths(successors, topologicalOrder, limits);
        return new ValidatedActionGraph(topologicalOrder, successors);
    }

    private static void requireActionCount(Map<Long, List<Long>> successors, ActionGraphLimits limits) {
        if (successors.isEmpty()) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.ACTION_COUNT,
                    "Action graph must contain at least one action");
        }
        if (successors.size() > limits.maximumActions()) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.ACTION_COUNT,
                    "Action graph exceeds maximum actions: " + limits.maximumActions());
        }
    }

    private static Map<Long, Integer> countIncomingEdges(Map<Long, List<Long>> successors) {
        Map<Long, Integer> remainingPredecessorsByActionId = new LinkedHashMap<>();
        successors.keySet()
                  .forEach(id -> {
                      if (id == null) {
                          throw new ActionGraphValidationException(ActionGraphValidationException.Reason.IDENTIFIER,
                                  "Action identifier is required");
                      }
                      remainingPredecessorsByActionId.put(id, 0);
                  });
        successors.forEach((id, next) -> {
            if (next == null || new HashSet<>(next).size() != next.size()) {
                throw new ActionGraphValidationException(ActionGraphValidationException.Reason.SUCCESSORS,
                        "Action successors must be present and unique: " + id);
            }
            for (Long successor : next) {
                if (successor == null || !remainingPredecessorsByActionId.containsKey(successor)) {
                    throw new ActionGraphValidationException(ActionGraphValidationException.Reason.UNKNOWN_SUCCESSOR,
                            "Unknown action successor: " + successor);
                }
                remainingPredecessorsByActionId.compute(successor, (_, count) -> Math.addExact(count, 1));
            }
        });
        return remainingPredecessorsByActionId;
    }

    private static List<Long> orderActions(Map<Long, List<Long>> successors,
            Map<Long, Integer> remainingPredecessorsByActionId) {
        ArrayDeque<Long> actionsReadyForOrdering = new ArrayDeque<>();
        remainingPredecessorsByActionId.forEach((id, count) -> {
            if (count == 0) {
                actionsReadyForOrdering.addLast(id);
            }
        });
        int startingActions = actionsReadyForOrdering.size();
        List<Long> topologicalOrder = new ArrayList<>();
        while (!actionsReadyForOrdering.isEmpty()) {
            Long id = actionsReadyForOrdering.removeFirst();
            topologicalOrder.add(id);
            for (Long successor : successors.get(id)) {
                if (remainingPredecessorsByActionId.compute(successor, (_, count) -> count - 1) == 0) {
                    actionsReadyForOrdering.addLast(successor);
                }
            }
        }
        if (topologicalOrder.size() != successors.size()) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.CYCLE,
                    "Action graph must not contain cycles");
        }
        if (startingActions != 1) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.STARTING_ACTION,
                    "Action graph must have exactly one starting action");
        }
        return topologicalOrder;
    }

    private static void requireBoundedPaths(Map<Long, List<Long>> successors, List<Long> order,
            ActionGraphLimits limits) {
        Map<Long, Long> pathCountsByActionId = new HashMap<>();
        Map<Long, Long> occurrenceCountsByActionId = new HashMap<>();
        for (Long id : order.reversed()) {
            List<Long> successorActionIds = successors.get(id);
            long pathCount = 0;
            long occurrenceCount = 0;
            if (successorActionIds.isEmpty()) {
                pathCount = 1;
                occurrenceCount = 1;
            }
            for (Long successor : successorActionIds) {
                pathCount += pathCountsByActionId.get(successor);
                // Every path through a successor also executes this action once.
                occurrenceCount += occurrenceCountsByActionId.get(successor) + pathCountsByActionId.get(successor);
                requireWithinLimits(pathCount, occurrenceCount, limits);
            }
            requireWithinLimits(pathCount, occurrenceCount, limits);
            pathCountsByActionId.put(id, pathCount);
            occurrenceCountsByActionId.put(id, occurrenceCount);
        }
    }

    private static void requireWithinLimits(long paths, long occurrences, ActionGraphLimits limits) {
        if (paths > limits.maximumPaths()) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.PATH_COUNT,
                    "Action graph exceeds maximum paths: " + limits.maximumPaths());
        }
        if (occurrences > limits.maximumActionOccurrences()) {
            throw new ActionGraphValidationException(ActionGraphValidationException.Reason.ACTION_OCCURRENCES,
                    "Action graph exceeds maximum action occurrences: " + limits.maximumActionOccurrences());
        }
    }
}
