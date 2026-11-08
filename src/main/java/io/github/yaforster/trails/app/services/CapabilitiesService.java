package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.Capability;

import java.util.List;

@FunctionalInterface
public interface CapabilitiesService {

    List<Capability> capabilities();

}
