package com.vertex.pm.service;

import com.vertex.pm.exception.DuplicateProjectException;
import com.vertex.pm.exception.InvalidTaskDateException;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.exception.ProjectNotFoundException;
import com.vertex.pm.exception.ResourceNotAvailableException;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Risk;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;
import com.vertex.pm.repository.ProjectRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles project-related business logic.
 * SRP: this class contains only business rules and validation.
 */
public class ProjectService {
    private final ProjectRepository projectRepository;

    /**
     * Creates the project service.
     */
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Retrieves all projects.
     */
    public List<Project> getAllProjects() throws ProjectManagementException {
        return projectRepository.getAllProjects();
    }

    /**
     * Retrieves one project by id.
     */
    public Project getProjectById(int projectId) throws ProjectManagementException {
        return projectRepository.getProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found for id " + projectId));
    }

    /**
     * Creates a new project after validation.
     */
    public void createProject(Project project) throws ProjectManagementException {
        validateProject(project);
        projectRepository.saveProject(project);
    }

    /**
     * Updates an existing project after validation.
     */
    public void updateProject(Project project) throws ProjectManagementException {
        if (projectRepository.getProjectById(project.getId()).isEmpty()) {
            throw new ProjectNotFoundException("Project not found for id " + project.getId());
        }
        validateProjectForUpdate(project);
        projectRepository.updateProject(project);
    }

    /**
     * Deletes a project by id.
     */
    public void deleteProject(int projectId) throws ProjectManagementException {
        getProjectById(projectId);
        projectRepository.deleteProject(projectId);
    }

    /**
     * Returns all tasks from every project.
     */
    public List<Task> getAllTasks() throws ProjectManagementException {
        List<Task> tasks = new ArrayList<>();
        for (Project project : projectRepository.getAllProjects()) {
            tasks.addAll(project.getTasks());
        }
        return tasks;
    }

    /**
     * Returns all resources from every project.
     */
    public List<Resource> getAllResources() throws ProjectManagementException {
        List<Resource> resources = new ArrayList<>();
        for (Project project : projectRepository.getAllProjects()) {
            resources.addAll(project.getResources());
        }
        return resources;
    }

    /**
     * Returns all milestones from every project.
     */
    public List<Milestone> getAllMilestones() throws ProjectManagementException {
        List<Milestone> milestones = new ArrayList<>();
        for (Project project : projectRepository.getAllProjects()) {
            milestones.addAll(project.getMilestones());
        }
        return milestones;
    }

    /**
     * Returns all expenses from every project.
     */
    public List<Expense> getAllExpenses() throws ProjectManagementException {
        List<Expense> expenses = new ArrayList<>();
        for (Project project : projectRepository.getAllProjects()) {
            expenses.addAll(project.getBudget().getExpenses());
        }
        return expenses;
    }

    /**
     * Adds a task into a project.
     */
    public void addTask(Task task) throws ProjectManagementException {
        Project project = getProjectById(task.getProjectId());
        if (task.getStartDate().isBefore(project.getStartDate()) || task.getEndDate().isAfter(project.getEndDate())) {
            throw new InvalidTaskDateException("Task dates must stay within the project duration.");
        }
        project.addTask(task);
        projectRepository.updateProject(project);
    }

    /**
     * Updates a task after validation.
     */
    public void updateTask(Task updatedTask) throws ProjectManagementException {
        Project project = findProjectByTaskId(updatedTask.getId());
        Task task = project.findTask(updatedTask.getId());
        if (updatedTask.getStartDate().isBefore(project.getStartDate()) || updatedTask.getEndDate().isAfter(project.getEndDate())) {
            throw new InvalidTaskDateException("Task dates must stay within the project duration.");
        }
        task.setName(updatedTask.getName());
        task.setStartDate(updatedTask.getStartDate());
        task.setEndDate(updatedTask.getEndDate());
        task.setStatus(updatedTask.getStatus());
        projectRepository.updateProject(project);
    }

    /**
     * Updates a task status.
     */
    public void updateTaskStatus(int taskId, TaskStatus status) throws ProjectManagementException {
        Project project = findProjectByTaskId(taskId);
        Task task = project.findTask(taskId);
        task.setStatus(status);
        projectRepository.updateProject(project);
    }

    /**
     * Deletes a task by id.
     */
    public void deleteTask(int taskId) throws ProjectManagementException {
        Project project = findProjectByTaskId(taskId);
        project.removeTask(taskId);
        projectRepository.updateProject(project);
    }

    /**
     * Adds a resource into a project.
     */
    public void addResource(int projectId, Resource resource) throws ProjectManagementException {
        Project project = getProjectById(projectId);
        project.addResource(resource);
        projectRepository.updateProject(project);
    }

    /**
     * Updates a resource in place.
     */
    public void updateResource(Resource updatedResource) throws ProjectManagementException {
        Project project = findProjectByResourceId(updatedResource.getId());
        Resource resource = project.findResource(updatedResource.getId());
        resource.setName(updatedResource.getName());
        resource.setRole(updatedResource.getRole());
        resource.setAvailable(updatedResource.isAvailable());
        projectRepository.updateProject(project);
    }

    /**
     * Deletes a resource by id.
     */
    public void deleteResource(int resourceId) throws ProjectManagementException {
        Project project = findProjectByResourceId(resourceId);
        project.removeResource(resourceId);
        projectRepository.updateProject(project);
    }

    /**
     * Adds an expense to a project budget.
     */
    public void addExpense(Expense expense) throws ProjectManagementException {
        Project project = getProjectById(expense.getProjectId());
        project.getBudget().addExpense(expense);
        projectRepository.updateProject(project);
    }

    /**
     * Updates an expense.
     */
    public void updateExpense(Expense updatedExpense) throws ProjectManagementException {
        Project project = findProjectByExpenseId(updatedExpense.getId());
        Expense expense = project.findExpense(updatedExpense.getId());
        expense.setAmount(updatedExpense.getAmount());
        expense.setCategory(updatedExpense.getCategory());
        expense.setDate(updatedExpense.getDate());
        projectRepository.updateProject(project);
    }

    /**
     * Adds a milestone to a project.
     */
    public void addMilestone(Milestone milestone) throws ProjectManagementException {
        Project project = getProjectById(milestone.getProjectId());
        project.addMilestone(milestone);
        projectRepository.updateProject(project);
    }

    /**
     * Updates a milestone.
     */
    public void updateMilestone(Milestone updatedMilestone) throws ProjectManagementException {
        Project project = findProjectByMilestoneId(updatedMilestone.getId());
        Milestone milestone = project.findMilestone(updatedMilestone.getId());
        milestone.setName(updatedMilestone.getName());
        milestone.setTargetDate(updatedMilestone.getTargetDate());
        milestone.setCompleted(updatedMilestone.isCompleted());
        projectRepository.updateProject(project);
    }

    /**
     * Deletes an expense.
     */
    public void deleteExpense(int expenseId) throws ProjectManagementException {
        Project project = findProjectByExpenseId(expenseId);
        project.getBudget().removeExpense(expenseId);
        projectRepository.updateProject(project);
    }

    /**
     * Marks a milestone completed.
     */
    public void completeMilestone(int milestoneId) throws ProjectManagementException {
        Project project = findProjectByMilestoneId(milestoneId);
        Milestone milestone = project.findMilestone(milestoneId);
        milestone.setCompleted(true);
        projectRepository.updateProject(project);
    }

    /**
     * Deletes a milestone.
     */
    public void deleteMilestone(int milestoneId) throws ProjectManagementException {
        Project project = findProjectByMilestoneId(milestoneId);
        project.removeMilestone(milestoneId);
        projectRepository.updateProject(project);
    }

    /**
     * Builds dashboard summary metrics.
     */
    public Map<String, Object> getDashboardSummary() throws ProjectManagementException {
        List<Project> projects = projectRepository.getAllProjects();
        int totalProjects = projects.size();
        long activeProjects = projects.stream().filter(project -> project.getStatus().name().equals("IN_PROGRESS")).count();
        long completedProjects = projects.stream().filter(project -> project.getStatus().name().equals("COMPLETED")).count();
        double totalBudget = projects.stream().mapToDouble(project -> project.getBudget().getTotalAmount()).sum();
        double spentBudget = projects.stream().mapToDouble(project -> project.getBudget().getSpentAmount()).sum();
        long riskAlerts = projects.stream().flatMap(project -> project.getRisks().stream()).count();

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProjects", totalProjects);
        summary.put("activeProjects", activeProjects);
        summary.put("completedProjects", completedProjects);
        summary.put("budgetUsagePercent", totalBudget == 0 ? 0 : Math.round((spentBudget / totalBudget) * 100));
        summary.put("riskAlerts", riskAlerts);
        return summary;
    }

    /**
     * Builds monitoring details for delayed tasks, budget overruns, and risk alerts.
     */
    public Map<String, Object> getMonitoringSummary() throws ProjectManagementException {
        List<Project> projects = projectRepository.getAllProjects();
        List<Task> delayedTasks = new ArrayList<>();
        List<Project> budgetOverruns = new ArrayList<>();
        List<Risk> risks = new ArrayList<>();

        for (Project project : projects) {
            for (Task task : project.getTasks()) {
                if (task.getEndDate().isBefore(java.time.LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED) {
                    delayedTasks.add(task);
                }
            }
            if (project.getBudget().getRemainingAmount() < 0) {
                budgetOverruns.add(project);
            }
            risks.addAll(project.getRisks());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("delayedTaskCount", delayedTasks.size());
        summary.put("budgetOverrunCount", budgetOverruns.size());
        summary.put("riskAlertCount", risks.size());
        summary.put("health", delayedTasks.isEmpty() && budgetOverruns.isEmpty() ? "Healthy" : "Warning");
        return summary;
    }

    /**
     * Builds report data.
     */
    public Map<String, Object> getReportsSummary() throws ProjectManagementException {
        List<Project> projects = projectRepository.getAllProjects();
        Map<String, Object> reports = new LinkedHashMap<>();
        reports.put("projectReports", projects.size());
        reports.put("taskReports", getAllTasks().size());
        reports.put("budgetReports", projects.stream().map(Project::getBudget).count());
        return reports;
    }

    /**
     * Validates business rules before persistence.
     */
    public void validateProject(Project project) throws ProjectManagementException {
        if (project == null) {
            throw new ProjectNotFoundException("Project payload cannot be null.");
        }
        if (project.getStartDate().isAfter(project.getEndDate())) {
            throw new InvalidTaskDateException("Project start date must be before end date.");
        }
        for (Project existing : projectRepository.getAllProjects()) {
            if (existing.getName().equalsIgnoreCase(project.getName())) {
                throw new DuplicateProjectException("Project name already exists: " + project.getName());
            }
        }
        for (Task task : project.getTasks()) {
            if (task.getStartDate().isBefore(project.getStartDate()) || task.getEndDate().isAfter(project.getEndDate())) {
                throw new InvalidTaskDateException("Task dates must stay within the project duration.");
            }
        }
        boolean hasUnavailableResource = project.getResources().stream().anyMatch(resource -> !resource.isAvailable());
        if (hasUnavailableResource) {
            throw new ResourceNotAvailableException("One or more assigned resources are not available.");
        }
    }

    private void validateProjectForUpdate(Project project) throws ProjectManagementException {
        if (project == null) {
            throw new ProjectNotFoundException("Project payload cannot be null.");
        }
        if (project.getStartDate().isAfter(project.getEndDate())) {
            throw new InvalidTaskDateException("Project start date must be before end date.");
        }
        for (Project existing : projectRepository.getAllProjects()) {
            if (existing.getId() != project.getId() && existing.getName().equalsIgnoreCase(project.getName())) {
                throw new DuplicateProjectException("Project name already exists: " + project.getName());
            }
        }
    }

    private Project findProjectByTaskId(int taskId) throws ProjectManagementException {
        for (Project project : projectRepository.getAllProjects()) {
            if (project.findTask(taskId) != null) {
                return project;
            }
        }
        throw new ProjectNotFoundException("Task not found for id " + taskId);
    }

    private Project findProjectByResourceId(int resourceId) throws ProjectManagementException {
        for (Project project : projectRepository.getAllProjects()) {
            if (project.getResources().stream().anyMatch(resource -> resource.getId() == resourceId)) {
                return project;
            }
        }
        throw new ProjectNotFoundException("Resource not found for id " + resourceId);
    }

    private Project findProjectByExpenseId(int expenseId) throws ProjectManagementException {
        for (Project project : projectRepository.getAllProjects()) {
            if (project.getBudget().getExpenses().stream().anyMatch(expense -> expense.getId() == expenseId)) {
                return project;
            }
        }
        throw new ProjectNotFoundException("Expense not found for id " + expenseId);
    }

    private Project findProjectByMilestoneId(int milestoneId) throws ProjectManagementException {
        for (Project project : projectRepository.getAllProjects()) {
            if (project.findMilestone(milestoneId) != null) {
                return project;
            }
        }
        throw new ProjectNotFoundException("Milestone not found for id " + milestoneId);
    }
}
