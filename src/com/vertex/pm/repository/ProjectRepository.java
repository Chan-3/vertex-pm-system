package com.vertex.pm.repository;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Project;

import java.util.List;
import java.util.Optional;

/**
 * Defines repository operations for project access.
 * OCP and DIP: services depend on this abstraction rather than a concrete data source.
 */
public interface ProjectRepository {
    /**
     * Retrieves all projects.
     */
    List<Project> getAllProjects() throws ProjectManagementException;

    /**
     * Retrieves a project by id.
     */
    Optional<Project> getProjectById(int projectId) throws ProjectManagementException;

    /**
     * Saves a project.
     */
    void saveProject(Project project) throws ProjectManagementException;

    /**
     * Updates an existing project.
     */
    void updateProject(Project project) throws ProjectManagementException;

    /**
     * Deletes a project by id.
     */
    void deleteProject(int projectId) throws ProjectManagementException;
}
