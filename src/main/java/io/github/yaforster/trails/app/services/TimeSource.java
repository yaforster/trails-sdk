package io.github.yaforster.trails.app.services;

@FunctionalInterface
public interface TimeSource {

    long currentTimeMillis();

}
