package com.vertex.pm.repository;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectReport;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Task;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProjectRepository {
    /** Returns all projects with their linked child data. */
    List<Project> getAllProjects() throws ProjectManagementException;
    /** Returns one project if it exists. */
    Optional<Project> getProjectById(String projectId) throws ProjectManagementException;
    /** Persists a new project and its starting budget. */
    Project createProject(Project project) throws ProjectManagementException;
    /** Applies a partial update to project fields without replacing the full object. */
    Project patchProject(String projectId, Map<String, Object> changes) throws ProjectManagementException;
    /** Updates only progress-related project fields such as status and percentage. */
    Project patchProjectProgress(String projectId, Map<String, Object> changes) throws ProjectManagementException;
    /** Deletes a project by identifier. */
    void deleteProject(String projectId) throws ProjectManagementException;
    /** Returns every task across all projects. */
    List<Task> getAllTasks() throws ProjectManagementException;
    /** Persists a new task. */
    Task createTask(Task task) throws ProjectManagementException;
    /** Applies partial changes to a task. */
    Task patchTask(String taskId, Map<String, Object> changes) throws ProjectManagementException;
    /** Deletes a task by identifier. */
    void deleteTask(String taskId) throws ProjectManagementException;
    /** Returns all resources. */
    List<Resource> getAllResources() throws ProjectManagementException;
    /** Persists a new resource. */
    Resource createResource(Resource resource) throws ProjectManagementException;
    /** Applies partial changes to a resource. */
    Resource patchResource(String resourceId, Map<String, Object> changes) throws ProjectManagementException;
    /** Deletes a resource by identifier. */
    void deleteResource(String resourceId) throws ProjectManagementException;
    /** Returns all milestones. */
    List<Milestone> getAllMilestones() throws ProjectManagementException;
    /** Persists a new milestone. */
    Milestone createMilestone(Milestone milestone) throws ProjectManagementException;
    /** Applies partial changes to a milestone. */
    Milestone patchMilestone(String milestoneId, Map<String, Object> changes) throws ProjectManagementException;
    /** Deletes a milestone by identifier. */
    void deleteMilestone(String milestoneId) throws ProjectManagementException;
    /** Returns all expenses. */
    List<Expense> getAllExpenses() throws ProjectManagementException;
    /** Persists a new expense. */
    Expense createExpense(Expense expense) throws ProjectManagementException;
    /** Applies partial changes to an expense. */
    Expense patchExpense(String expenseId, Map<String, Object> changes) throws ProjectManagementException;
    /** Deletes an expense by identifier. */
    void deleteExpense(String expenseId) throws ProjectManagementException;
    /** Builds the detailed report payload used by the report screen. */
    ProjectReport getProjectReport(String projectId) throws ProjectManagementException;
}
