package io.github.yaforster.trails.core;

public record ValidationViolation(String code, String message, String path) {
}
