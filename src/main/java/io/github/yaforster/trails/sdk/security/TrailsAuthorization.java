package io.github.yaforster.trails.sdk.security;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import java.util.Objects;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

/**
 * Applies Trails-owned authorization rules independently from the selected authentication mechanism.
 */
public final class TrailsAuthorization {

    private final boolean authenticationRequired;
    private final TrailsSecurityProperties.Roles roles;

    public TrailsAuthorization(TrailsComposition composition, TrailsSecurityProperties properties) {
        this.authenticationRequired = Objects.requireNonNull(composition, "Trails composition is required")
                                             .authenticationRequired();
        this.roles = requireRoles(Objects.requireNonNull(properties, "Trails security properties are required")
                                         .roles());
    }

    public boolean canManageResources(Authentication authentication) {
        return isPermitted(authentication, roles.testManager(), roles.admin());
    }

    public boolean canPromoteResources() {
        return canManageResources(currentAuthentication());
    }

    public boolean canManageResources() {
        return canManageResources(currentAuthentication());
    }

    public boolean canAccessTestData(Authentication authentication) {
        return isPermitted(authentication, roles.tester(), roles.testManager(), roles.admin());
    }

    public boolean canAccessTestData() {
        return canAccessTestData(currentAuthentication());
    }

    public boolean canExecuteTests(Authentication authentication) {
        return isPermitted(authentication, roles.tester(), roles.testManager(), roles.admin());
    }

    public boolean canExecuteTests() {
        return canExecuteTests(currentAuthentication());
    }

    public boolean canManageUsers(Authentication authentication) {
        return isPermitted(authentication, roles.admin());
    }

    public boolean canManageUsers() {
        return canManageUsers(currentAuthentication());
    }

    public boolean canReportDeployments(Authentication authentication) {
        return isPermitted(authentication, roles.deploymentReporter(), roles.admin());
    }

    public boolean canReportDeployments() {
        return canReportDeployments(currentAuthentication());
    }

    private boolean isPermitted(Authentication authentication, String... permittedRoles) {
        if (!authenticationRequired) {
            return true;
        }
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities()
                             .stream()
                             .map(GrantedAuthority::getAuthority)
                             .anyMatch(authority -> java.util.Arrays.asList(permittedRoles)
                                                                    .contains(authority));
    }

    private TrailsSecurityProperties.Roles requireRoles(TrailsSecurityProperties.Roles configuredRoles) {
        if (!authenticationRequired) {
            return Objects.requireNonNullElseGet(configuredRoles,
                    () -> new TrailsSecurityProperties.Roles("", "", "", ""));
        }
        if (configuredRoles == null || isBlank(configuredRoles.testManager()) || isBlank(configuredRoles.tester())
                || isBlank(configuredRoles.admin()) || isBlank(configuredRoles.deploymentReporter())) {
            throw new IllegalStateException(
                    "trails.security.roles.test-manager, tester, admin, and deployment-reporter "
                            + "must be configured for role-based authentication");
        }
        return configuredRoles;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Authentication currentAuthentication() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                                                                              .getAuthentication();
    }

}
