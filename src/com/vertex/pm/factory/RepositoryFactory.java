package com.vertex.pm.factory;

import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.repository.mysql.MySqlProjectRepository;

public final class RepositoryFactory {
    private RepositoryFactory() {
    }

    /** Returns the repository implementation used by the application at runtime. */
    public static ProjectRepository getProjectRepository() {
        return new MySqlProjectRepository();
    }
}
