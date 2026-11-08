package io.github.yaforster.trails.sdk.composition;

import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.app.services.ActionResultPersistence;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.app.services.DownloadedFilePersistence;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.TestPathResultPersistence;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultPersistence;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.app.services.UserProfileService;
import java.util.Objects;

/**
 * Registers one complete, atomic implementation of the Trails persistence port set.
 * <p>
 * The SDK consumes this registration rather than individually discovered persistence beans. Consequently, a customer
 * cannot accidentally combine parts of two adapters or allow Spring's primary-bean selection to choose an adapter.
 */
public record TrailsPersistenceRegistration(AdapterId adapterId, Ports ports) {

    public TrailsPersistenceRegistration {
        Objects.requireNonNull(adapterId, "Persistence adapter identifier is required");
        Objects.requireNonNull(ports, "Complete persistence ports are required");
    }

    /** The complete legacy persistence port set required by the Trails host. */
    public record Ports(ApplicationDatabaseService applicationDatabaseService,
            DeploymentDatabaseService deploymentDatabaseService, StageDatabaseService stageDatabaseService,
            ElementDatabaseService elementDatabaseService, ScreenshotDatabaseService screenshotDatabaseService,
            TestDataDatabaseService testDataDatabaseService, TestPlanDatabaseService testPlanDatabaseService,
            ActionQueryService actionQueryService, TestRunResultDatabaseService testRunResultDatabaseService,
            TestSetResultQueryService testSetResultQueryService, TestSetResultPersistence testSetResultPersistence,
            TestPathResultQueryService testPathResultQueryService, TestPathResultPersistence testPathResultPersistence,
            ActionResultQueryService actionResultQueryService, ActionResultPersistence actionResultPersistence,
            ActionResultScreenshotQueryService actionResultScreenshotQueryService,
            ArtifactQueryService artifactQueryService, DownloadedFilePersistence downloadedFilePersistence,
            UserProfileService userProfileService) {

        public Ports {
            requirePort(applicationDatabaseService, "Application database service");
            requirePort(deploymentDatabaseService, "Deployment database service");
            requirePort(stageDatabaseService, "Stage database service");
            requirePort(elementDatabaseService, "Element database service");
            requirePort(screenshotDatabaseService, "Screenshot database service");
            requirePort(testDataDatabaseService, "Test data database service");
            requirePort(testPlanDatabaseService, "Test plan database service");
            requirePort(actionQueryService, "Action query service");
            requirePort(testRunResultDatabaseService, "Test run result database service");
            requirePort(testSetResultQueryService, "Test set result query service");
            requirePort(testSetResultPersistence, "Test set result persistence");
            requirePort(testPathResultQueryService, "Test path result query service");
            requirePort(testPathResultPersistence, "Test path result persistence");
            requirePort(actionResultQueryService, "Action result query service");
            requirePort(actionResultPersistence, "Action result persistence");
            requirePort(actionResultScreenshotQueryService, "Action result screenshot query service");
            requirePort(artifactQueryService, "Artifact query service");
            requirePort(downloadedFilePersistence, "Downloaded file persistence");
            requirePort(userProfileService, "User profile service");
        }

        private static void requirePort(Object port, String portName) {
            Objects.requireNonNull(port, portName + " is required");
        }

    }

}
