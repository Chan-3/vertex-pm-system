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
    List<Project> getAllProjects() throws ProjectManagementException;
    Optional<Project> getProjectById(String projectId) throws ProjectManagementException;
    Project createProject(Project project) throws ProjectManagementException;
    Project patchProject(String projectId, Map<String, Object> changes) throws ProjectManagementException;
    Project patchProjectProgress(String projectId, Map<String, Object> changes) throws ProjectManagementException;
    void deleteProject(String projectId) throws ProjectManagementException;
    List<Task> getAllTasks() throws ProjectManagementException;
    Task createTask(Task task) throws ProjectManagementException;
    Task patchTask(String taskId, Map<String, Object> changes) throws ProjectManagementException;
    void deleteTask(String taskId) throws ProjectManagementException;
    List<Resource> getAllResources() throws ProjectManagementException;
    Resource createResource(Resource resource) throws ProjectManagementException;
    Resource patchResource(String resourceId, Map<String, Object> changes) throws ProjectManagementException;
    void deleteResource(String resourceId) throws ProjectManagementException;
    List<Milestone> getAllMilestones() throws ProjectManagementException;
    Milestone createMilestone(Milestone milestone) throws ProjectManagementException;
    Milestone patchMilestone(String milestoneId, Map<String, Object> changes) throws ProjectManagementException;
    void deleteMilestone(String milestoneId) throws ProjectManagementException;
    List<Expense> getAllExpenses() throws ProjectManagementException;
    Expense createExpense(Expense expense) throws ProjectManagementException;
    Expense patchExpense(String expenseId, Map<String, Object> changes) throws ProjectManagementException;
    void deleteExpense(String expenseId) throws ProjectManagementException;
    ProjectReport getProjectReport(String projectId) throws ProjectManagementException;
}
