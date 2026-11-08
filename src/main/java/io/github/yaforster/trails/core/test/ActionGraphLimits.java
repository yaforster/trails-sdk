package io.github.yaforster.trails.core.test;

/** Bounds graph preparation and the aggregate size of all root-to-leaf execution paths. */
public record ActionGraphLimits(int maximumActions, int maximumPaths, int maximumActionOccurrences) {
    public static final ActionGraphLimits DEFAULTS = new ActionGraphLimits(10_000, 1_000, 100_000);

    public ActionGraphLimits {
        if (maximumActions <= 0 || maximumPaths <= 0 || maximumActionOccurrences <= 0) {
            throw new IllegalArgumentException("Action graph limits must be positive");
        }
    }
}
