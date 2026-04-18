package com.vertex.pm.repository.api;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Project;
import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.util.CloudApiClient;

import java.util.List;
import java.util.Optional;

/**
 * Fetches project data from a cloud-style API source.
 * Strategy pattern: this is one interchangeable data access strategy.
 */
public class ApiProjectRepository implements ProjectRepository {
    private final CloudApiClient cloudApiClient;

    /**
     * Creates the API repository.
     */
    public ApiProjectRepository(CloudApiClient cloudApiClient) {
        this.cloudApiClient = cloudApiClient;
    }

    @Override
    public List<Project> getAllProjects() throws ProjectManagementException {
        return cloudApiClient.fetchProjects();
    }

    @Override
    public Optional<Project> getProjectById(int projectId) throws ProjectManagementException {
        return cloudApiClient.fetchProjects().stream().filter(project -> project.getId() == projectId).findFirst();
    }

    @Override
    public void saveProject(Project project) throws ProjectManagementException {
        cloudApiClient.saveProject(project);
    }

    @Override
    public void updateProject(Project project) throws ProjectManagementException {
        cloudApiClient.updateProject(project);
    }

    @Override
    public void deleteProject(int projectId) throws ProjectManagementException {
        cloudApiClient.deleteProject(projectId);
    }
}
