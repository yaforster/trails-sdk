package io.github.yaforster.trails.app.services;

import java.util.List;

@FunctionalInterface
public interface CapabilityContributor {

    List<CapabilityContribution> contribute();

}
