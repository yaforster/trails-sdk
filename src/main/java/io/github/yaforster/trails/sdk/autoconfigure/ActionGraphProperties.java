package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.core.test.ActionGraphLimits;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("trails.execution.graph")
public record ActionGraphProperties(Integer maximumActions, Integer maximumPaths, Integer maximumActionOccurrences) {
    public ActionGraphProperties {
        ActionGraphLimits defaults = ActionGraphLimits.DEFAULTS;
        maximumActions = Optional.ofNullable(maximumActions)
                                 .orElse(defaults.maximumActions());
        maximumPaths = Optional.ofNullable(maximumPaths)
                               .orElse(defaults.maximumPaths());
        maximumActionOccurrences = Optional.ofNullable(maximumActionOccurrences)
                                           .orElse(defaults.maximumActionOccurrences());
        new ActionGraphLimits(maximumActions, maximumPaths, maximumActionOccurrences);
    }

    ActionGraphLimits limits() {
        return new ActionGraphLimits(maximumActions, maximumPaths, maximumActionOccurrences);
    }
}
