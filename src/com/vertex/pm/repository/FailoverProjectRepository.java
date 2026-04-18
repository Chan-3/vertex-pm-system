package com.vertex.pm.repository;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.exception.RepositoryException;
import com.vertex.pm.model.Project;
import com.vertex.pm.util.AppLogger;

import java.util.List;
import java.util.Optional;

/**
 * Provides DB-to-API failover when the primary repository fails.
 * Strategy + Facade: wraps multiple strategies behind one repository interface.
 */
public class FailoverProjectRepository implements ProjectRepository {
    private final ProjectRepository primaryRepository;
    private final ProjectRepository fallbackRepository;

    /**
     * Creates the failover repository.
     */
    public FailoverProjectRepository(ProjectRepository primaryRepository, ProjectRepository fallbackRepository) {
        this.primaryRepository = primaryRepository;
        this.fallbackRepository = fallbackRepository;
    }

    @Override
    public List<Project> getAllProjects() throws ProjectManagementException {
        try {
            return primaryRepository.getAllProjects();
        } catch (ProjectManagementException ex) {
            AppLogger.warning("Primary repository failed while loading projects. Switching to API fallback.", ex);
            return fallbackRepository.getAllProjects();
        }
    }

    @Override
    public Optional<Project> getProjectById(int projectId) throws ProjectManagementException {
        try {
            return primaryRepository.getProjectById(projectId);
        } catch (ProjectManagementException ex) {
            AppLogger.warning("Primary repository failed while loading project by id. Switching to API fallback.", ex);
            return fallbackRepository.getProjectById(projectId);
        }
    }

    @Override
    public void saveProject(Project project) throws ProjectManagementException {
        try {
            primaryRepository.saveProject(project);
        } catch (ProjectManagementException ex) {
            AppLogger.warning("Primary repository failed while saving project. Switching to API fallback.", ex);
            try {
                fallbackRepository.saveProject(project);
            } catch (ProjectManagementException fallbackEx) {
                throw new RepositoryException("Both repositories failed while saving the project.", fallbackEx);
            }
        }
    }

    @Override
    public void updateProject(Project project) throws ProjectManagementException {
        try {
            primaryRepository.updateProject(project);
        } catch (ProjectManagementException ex) {
            AppLogger.warning("Primary repository failed while updating project. Switching to API fallback.", ex);
            fallbackRepository.updateProject(project);
        }
    }

    @Override
    public void deleteProject(int projectId) throws ProjectManagementException {
        try {
            primaryRepository.deleteProject(projectId);
        } catch (ProjectManagementException ex) {
            AppLogger.warning("Primary repository failed while deleting project. Switching to API fallback.", ex);
            fallbackRepository.deleteProject(projectId);
        }
    }
}
