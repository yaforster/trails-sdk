package io.github.yaforster.trails.sdk.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Trails-owned authorization role names, required only with an authentication adapter.
 *
 * @param roles
 *            configured Trails roles
 */
@ConfigurationProperties("trails.security")
public record TrailsSecurityProperties(Roles roles) {

    /**
     * Exact granted-authority values used by Trails-owned HTTP and method authorization.
     *
     * @param testManager
     *            resource-management authority
     * @param tester
     *            test-execution and test-data authority
     * @param admin
     *            administration authority
     * @param deploymentReporter
     *            deployment-reporting authority
     */
    public record Roles(String testManager, String tester, String admin, String deploymentReporter) {

    }

}
