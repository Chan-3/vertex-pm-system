package com.vertex.pm.factory;

import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.repository.mysql.MySqlProjectRepository;

public final class RepositoryFactory {
    private RepositoryFactory() {
    }

    public static ProjectRepository getProjectRepository() {
        return new MySqlProjectRepository();
    }
}
