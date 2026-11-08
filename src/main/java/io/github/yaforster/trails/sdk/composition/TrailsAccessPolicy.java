package io.github.yaforster.trails.sdk.composition;

/** SDK-owned authorization policy selected by an authentication registration. */
public enum TrailsAccessPolicy {
    /** Require an authenticated principal and configured Trails roles. */
    ROLE_BASED,
    /** Allow every Trails operation without credentials or role configuration. */
    UNRESTRICTED
}
