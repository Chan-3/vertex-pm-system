package com.vertex.pm.factory;

import com.vertex.pm.repository.FailoverProjectRepository;
import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.repository.api.ApiProjectRepository;
import com.vertex.pm.repository.mysql.MySqlProjectRepository;
import com.vertex.pm.util.CloudApiClient;
import com.vertex.pm.util.DatabaseConnectionManager;
import com.vertex.pm.util.EnvConfig;

/**
 * Creates repository instances and selects the active strategy.
 * Factory pattern: centralizes repository creation logic.
 */
public final class RepositoryFactory {
    private RepositoryFactory() {
    }

    /**
     * Returns a repository with MySQL as primary and API as fallback.
     */
    public static ProjectRepository getProjectRepository() {
        ProjectRepository apiRepository = new ApiProjectRepository(new CloudApiClient());
        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
        ProjectRepository mySqlRepository = new MySqlProjectRepository(connectionManager);
        String sourcePreference = EnvConfig.get("DATA_SOURCE", "auto").toLowerCase();
        if ("cloud".equals(sourcePreference)) {
            return apiRepository;
        }
        if ("local".equals(sourcePreference)) {
            return connectionManager.isDatabaseAvailable()
                    ? mySqlRepository
                    : apiRepository;
        }
        if (connectionManager.isDatabaseAvailable()) {
            return new FailoverProjectRepository(mySqlRepository, apiRepository);
        }
        return apiRepository;
    }
}
