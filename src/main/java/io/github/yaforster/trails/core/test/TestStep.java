package io.github.yaforster.trails.core.test;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

public record TestStep(Action action, List<TestStep> nextTestSteps) {
    public TestStep {
        nextTestSteps = List.copyOf(nextTestSteps);
    }

    /** Enumerates paths iteratively so long validated action chains do not consume the call stack. */
    private List<List<TestStep>> getPathsFrom(TestStep step) {
        List<List<TestStep>> paths = new ArrayList<>();
        List<TestStep> path = new ArrayList<>();
        ArrayDeque<Iterator<TestStep>> branches = new ArrayDeque<>();
        branches.push(List.of(step)
                          .iterator());
        while (!branches.isEmpty()) {
            Iterator<TestStep> branch = branches.peek();
            if (!branch.hasNext()) {
                // Each exhausted branch corresponds to the last step still retained in the active path.
                branches.pop();
                if (!path.isEmpty()) {
                    path.removeLast();
                }
                continue;
            }
            TestStep current = branch.next();
            path.add(current);
            if (current.nextTestSteps()
                       .isEmpty()) {
                paths.add(List.copyOf(path));
                path.removeLast();
            } else {
                branches.push(current.nextTestSteps()
                                     .iterator());
            }
        }
        return List.copyOf(paths);
    }

    public List<List<TestStep>> getTestPathsFromThisStep() {
        return getPathsFrom(this);
    }
}
