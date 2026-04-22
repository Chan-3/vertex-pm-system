package com.vertex.pm.service;

import com.vertex.pm.exception.ExceptionType;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectReport;
import com.vertex.pm.model.ProjectStatus;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;
import com.vertex.pm.repository.ProjectRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProjectService {
    // Low Coupling + Dependency Inversion: business logic depends on the repository abstraction.
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /** Returns all projects for the Projects screen. */
    public List<Project> getAllProjects() throws ProjectManagementException { return projectRepository.getAllProjects(); }
    /** Returns all tasks for the Tasks screen and related validations. */
    public List<Task> getAllTasks() throws ProjectManagementException { return projectRepository.getAllTasks(); }
    /** Returns all resources for the Resources screen. */
    public List<Resource> getAllResources() throws ProjectManagementException { return projectRepository.getAllResources(); }
    /** Returns all milestones for the Milestones screen. */
    public List<Milestone> getAllMilestones() throws ProjectManagementException { return projectRepository.getAllMilestones(); }
    /** Returns all expenses for the Expenses screen. */
    public List<Expense> getAllExpenses() throws ProjectManagementException { return projectRepository.getAllExpenses(); }

    /** Loads one project or raises PROJECT_NOT_FOUND when the id is invalid. */
    public Project getProjectById(String projectId) throws ProjectManagementException {
        return projectRepository.getProjectById(projectId)
                .orElseThrow(() -> new ProjectManagementException(ExceptionType.PROJECT_NOT_FOUND, "Project not found: " + projectId, 404));
    }

    /** Creates a project after validating the project schedule. */
    public Project createProject(Project project) throws ProjectManagementException {
        validateProjectDates(project.getStartDate(), project.getEndDate());
        Project created = projectRepository.createProject(project);
        return syncProjectProgress(created.getId());
    }

    /** Applies a partial project update without overwriting untouched values. */
    public Project patchProject(String projectId, Map<String, Object> changes) throws ProjectManagementException {
        Project existingProject = getProjectById(projectId);
        validateProjectPatch(existingProject, changes);
        return projectRepository.patchProject(projectId, changes);
    }

    /** Forces progress and status to be recomputed from tasks and milestones. */
    public Project patchProjectProgress(String projectId, Map<String, Object> changes) throws ProjectManagementException {
        getProjectById(projectId);
        return syncProjectProgress(projectId);
    }

    /** Deletes a project after first verifying that it exists. */
    public void deleteProject(String projectId) throws ProjectManagementException {
        getProjectById(projectId);
        projectRepository.deleteProject(projectId);
    }

    /** Creates a task after validating project schedule boundaries and assigned resource state. */
    public Task createTask(Task task) throws ProjectManagementException {
        Project project = getProjectById(task.getProjectId());
        validateTaskDates(project, task.getStartDate(), task.getDueDate());
        validateResourceAvailability(project, task.getAssignedTo());
        Task created = projectRepository.createTask(task);
        syncProjectProgress(task.getProjectId());
        return created;
    }

    /** Applies partial task changes and then refreshes project progress. */
    public Task patchTask(String taskId, Map<String, Object> changes) throws ProjectManagementException {
        Task existingTask = findTask(taskId);
        Project project = getProjectById(existingTask.getProjectId());
        if (existingTask.getStatus() == TaskStatus.COMPLETED
                && TaskStatus.COMPLETED.name().equals(String.valueOf(changes.get("status")))) {
            throw new ProjectManagementException(ExceptionType.TASK_ALREADY_COMPLETED, "Task is already completed.", 409);
        }
        LocalDate patchedStart = changes.containsKey("startDate") ? LocalDate.parse(String.valueOf(changes.get("startDate"))) : existingTask.getStartDate();
        LocalDate patchedDue = changes.containsKey("dueDate") ? LocalDate.parse(String.valueOf(changes.get("dueDate"))) : existingTask.getDueDate();
        validateTaskDates(project, patchedStart, patchedDue);
        String assignedTo = changes.containsKey("assignedTo") ? String.valueOf(changes.get("assignedTo")) : existingTask.getAssignedTo();
        validateResourceAvailability(project, assignedTo);
        Task patchedTask = projectRepository.patchTask(taskId, changes);
        syncProjectProgress(existingTask.getProjectId());
        return patchedTask;
    }

    /** Deletes a task and refreshes the parent project's progress. */
    public void deleteTask(String taskId) throws ProjectManagementException {
        Task existingTask = findTask(taskId);
        projectRepository.deleteTask(taskId);
        syncProjectProgress(existingTask.getProjectId());
    }
    /** Creates a resource under an existing project. */
    public Resource createResource(Resource resource) throws ProjectManagementException { getProjectById(resource.getProjectId()); return projectRepository.createResource(resource); }
    /** Applies partial resource changes. */
    public Resource patchResource(String resourceId, Map<String, Object> changes) throws ProjectManagementException { return projectRepository.patchResource(resourceId, changes); }
    /** Deletes a resource by identifier. */
    public void deleteResource(String resourceId) throws ProjectManagementException { projectRepository.deleteResource(resourceId); }
    /** Creates a milestone and recalculates project progress. */
    public Milestone createMilestone(Milestone milestone) throws ProjectManagementException {
        getProjectById(milestone.getProjectId());
        Milestone created = projectRepository.createMilestone(milestone);
        syncProjectProgress(milestone.getProjectId());
        return created;
    }
    /** Applies milestone changes and recalculates project progress. */
    public Milestone patchMilestone(String milestoneId, Map<String, Object> changes) throws ProjectManagementException {
        Milestone existingMilestone = findMilestone(milestoneId);
        Milestone patchedMilestone = projectRepository.patchMilestone(milestoneId, changes);
        syncProjectProgress(existingMilestone.getProjectId());
        return patchedMilestone;
    }
    /** Deletes a milestone and refreshes project progress. */
    public void deleteMilestone(String milestoneId) throws ProjectManagementException {
        Milestone existingMilestone = findMilestone(milestoneId);
        projectRepository.deleteMilestone(milestoneId);
        syncProjectProgress(existingMilestone.getProjectId());
    }
    /** Creates an expense for an existing project. */
    public Expense createExpense(Expense expense) throws ProjectManagementException { getProjectById(expense.getProjectId()); return projectRepository.createExpense(expense); }
    /** Applies partial changes to an expense row. */
    public Expense patchExpense(String expenseId, Map<String, Object> changes) throws ProjectManagementException { return projectRepository.patchExpense(expenseId, changes); }
    /** Deletes an expense row. */
    public void deleteExpense(String expenseId) throws ProjectManagementException { projectRepository.deleteExpense(expenseId); }
    /** Returns the project report payload used by the Reports screen. */
    public ProjectReport getProjectReport(String projectId) throws ProjectManagementException {
        syncProjectProgress(projectId);
        return projectRepository.getProjectReport(projectId);
    }

    /** Builds summary values for the dashboard cards. */
    public Map<String, Object> getDashboardSummary() throws ProjectManagementException {
        List<Project> projects = projectRepository.getAllProjects();
        long inProgress = projects.stream().filter(project -> project.getStatus() == ProjectStatus.IN_PROGRESS).count();
        long completed = projects.stream().filter(project -> project.getStatus() == ProjectStatus.COMPLETED).count();
        long overdueTasks = projectRepository.getAllTasks().stream()
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED)
                .count();
        double totalBudget = projects.stream().mapToDouble(project -> project.getBudget().getTotalAmount()).sum();
        double totalSpent = projects.stream().mapToDouble(project -> project.getBudget().getSpentAmount()).sum();
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalProjects", projects.size());
        summary.put("inProgressProjects", inProgress);
        summary.put("completedProjects", completed);
        summary.put("overdueTasks", overdueTasks);
        summary.put("budgetUsagePercent", totalBudget == 0 ? 0 : Math.round((totalSpent / totalBudget) * 100));
        return summary;
    }

    /** Builds lightweight monitoring metrics such as delayed tasks and blocked milestones. */
    public Map<String, Object> getMonitoringSummary() throws ProjectManagementException {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("delayedTaskCount", getAllTasks().stream()
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED)
                .count());
        summary.put("projectsAtRisk", getAllProjects().stream().filter(project -> project.getProgressPercent() < 50).count());
        summary.put("blockedMilestones", getAllMilestones().stream().filter(milestone -> !milestone.isCompletionStatus()).count());
        return summary;
    }

    /** Ensures the project date range is logically valid. */
    private void validateProjectDates(LocalDate startDate, LocalDate endDate) throws ProjectManagementException {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new ProjectManagementException(ExceptionType.INVALID_PROJECT_DATES, "Project start date must be before end date.", 400);
        }
    }

    /** Validates patched project dates by combining old values with incoming changes. */
    private void validateProjectPatch(Project existingProject, Map<String, Object> changes) throws ProjectManagementException {
        LocalDate startDate = changes.containsKey("startDate")
                ? LocalDate.parse(String.valueOf(changes.get("startDate")))
                : existingProject.getStartDate();
        LocalDate endDate = changes.containsKey("endDate")
                ? LocalDate.parse(String.valueOf(changes.get("endDate")))
                : existingProject.getEndDate();
        validateProjectDates(startDate, endDate);
    }

    /** Ensures task dates stay within the owning project's schedule window. */
    private void validateTaskDates(Project project, LocalDate startDate, LocalDate dueDate) throws ProjectManagementException {
        if (startDate == null || dueDate == null || startDate.isAfter(dueDate)
                || startDate.isBefore(project.getStartDate()) || dueDate.isAfter(project.getEndDate())) {
            throw new ProjectManagementException(ExceptionType.INVALID_TASK_DATES, "Task dates must stay within the project schedule.", 400);
        }
    }

    /** Prevents assigning tasks to resources currently marked unavailable. */
    private void validateResourceAvailability(Project project, String assignedTo) throws ProjectManagementException {
        if (assignedTo == null || assignedTo.isBlank()) {
            return;
        }
        boolean available = project.getResources().stream()
                .filter(resource -> resource.getName().equalsIgnoreCase(assignedTo))
                .findFirst()
                .map(Resource::isAvailability)
                .orElse(true);
        if (!available) {
            throw new ProjectManagementException(ExceptionType.RESOURCE_NOT_AVAILABLE, "Selected resource is currently unavailable.", 409);
        }
    }

    /** Finds a task from the current task list or throws TASK_NOT_FOUND. */
    private Task findTask(String taskId) throws ProjectManagementException {
        return getAllTasks().stream()
                .filter(task -> task.getId().equals(taskId))
                .findFirst()
                .orElseThrow(() -> new ProjectManagementException(ExceptionType.TASK_NOT_FOUND, "Task not found: " + taskId, 404));
    }

    /** Finds a milestone from the current milestone list or throws MILESTONE_NOT_FOUND. */
    private Milestone findMilestone(String milestoneId) throws ProjectManagementException {
        return getAllMilestones().stream()
                .filter(milestone -> milestone.getId().equals(milestoneId))
                .findFirst()
                .orElseThrow(() -> new ProjectManagementException(ExceptionType.MILESTONE_NOT_FOUND, "Milestone not found: " + milestoneId, 404));
    }

    private Project syncProjectProgress(String projectId) throws ProjectManagementException {
        // Information Expert: progress and status are calculated where task and milestone state is available.
        Project project = getProjectById(projectId);
        int taskCount = project.getTasks().size();
        int completedTasks = (int) project.getTasks().stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
        int milestoneCount = project.getMilestones().size();
        int completedMilestones = (int) project.getMilestones().stream().filter(Milestone::isCompletionStatus).count();

        int totalTrackers = taskCount + milestoneCount;
        int completedTrackers = completedTasks + completedMilestones;
        int progressPercent = totalTrackers == 0 ? 0 : (int) Math.round((completedTrackers * 100.0) / totalTrackers);

        ProjectStatus status;
        if (totalTrackers > 0 && completedTrackers == totalTrackers) {
            progressPercent = 100;
            status = ProjectStatus.COMPLETED;
        } else if (completedTrackers > 0) {
            status = ProjectStatus.IN_PROGRESS;
        } else {
            status = ProjectStatus.PLANNED;
        }

        Map<String, Object> progressChanges = new LinkedHashMap<>();
        progressChanges.put("progressPercent", progressPercent);
        progressChanges.put("status", status.name());
        return projectRepository.patchProjectProgress(projectId, progressChanges);
    }
}
