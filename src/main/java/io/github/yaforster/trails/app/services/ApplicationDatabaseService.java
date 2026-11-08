package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedApplication;

import java.util.Optional;

public interface ApplicationDatabaseService {

    PersistedApplication storeApplication(ApplicationDefinition applicationDefinition);

    PagedResult<PersistedApplication> getApplications(ApplicationPage applicationPage);

    Optional<PersistedApplication> getApplication(ApplicationDetails applicationDetails);

    boolean existsByLabel(String label);

    DatabaseDeletionResult deleteApplication(Long id);

    DatabaseDeletionResult restoreApplication(Long id);

    record ApplicationPage(int page, int size, boolean includeRetired) {
    }

    record ApplicationDetails(Long id, boolean includeRetired) {
    }

}
