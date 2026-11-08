package io.github.yaforster.trails.adapter.api.rest.validation;

@FunctionalInterface
public interface RequestValidator<T> {

    void validate(T request);

}
