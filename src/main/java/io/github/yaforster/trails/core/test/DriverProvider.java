package io.github.yaforster.trails.core.test;

import java.util.Optional;

@FunctionalInterface
public interface DriverProvider {

    Optional<BrowserExecution> createDriver();

}
