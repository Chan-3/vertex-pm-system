package com.vertex.pm.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a project entity.
 * GRASP Information Expert: the entity owns its project state and derived summary logic.
 */
public class Project {
    private final int id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private final Budget budget;
    private final List<Task> tasks = new ArrayList<>();
    private final List<Resource> resources = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();
    private final List<Risk> risks = new ArrayList<>();

    /**
     * Creates a project with its core details.
     */
    public Project(int id, String name, String description, LocalDate startDate, LocalDate endDate,
                   ProjectStatus status, Budget budget) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.budget = budget;
    }

    /**
     * Adds a task to the project.
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Removes a task from the project.
     */
    public boolean removeTask(int taskId) {
        return tasks.removeIf(task -> task.getId() == taskId);
    }

    /**
     * Adds a resource to the project.
     */
    public void addResource(Resource resource) {
        resources.add(resource);
    }

    /**
     * Removes a resource from the project.
     */
    public boolean removeResource(int resourceId) {
        return resources.removeIf(resource -> resource.getId() == resourceId);
    }

    /**
     * Adds a milestone to the project.
     */
    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
    }

    /**
     * Removes a milestone from the project.
     */
    public boolean removeMilestone(int milestoneId) {
        return milestones.removeIf(milestone -> milestone.getId() == milestoneId);
    }

    /**
     * Adds a dependency to the project.
     */
    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }

    /**
     * Adds a risk to the project.
     */
    public void addRisk(Risk risk) {
        risks.add(risk);
    }

    /**
     * Finds a task by id.
     */
    public Task findTask(int taskId) {
        return tasks.stream().filter(task -> task.getId() == taskId).findFirst().orElse(null);
    }

    /**
     * Finds a resource by id.
     */
    public Resource findResource(int resourceId) {
        return resources.stream().filter(resource -> resource.getId() == resourceId).findFirst().orElse(null);
    }

    /**
     * Finds a milestone by id.
     */
    public Milestone findMilestone(int milestoneId) {
        return milestones.stream().filter(milestone -> milestone.getId() == milestoneId).findFirst().orElse(null);
    }

    /**
     * Finds an expense by id.
     */
    public Expense findExpense(int expenseId) {
        return budget.getExpenses().stream().filter(expense -> expense.getId() == expenseId).findFirst().orElse(null);
    }

    /**
     * Calculates project completion percentage from task status.
     */
    public double getCompletionRatio() {
        if (tasks.isEmpty()) {
            return 0.0;
        }
        return tasks.stream().mapToDouble(Task::getCompletionRatio).average().orElse(0.0);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Updates the project name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Updates the project description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Updates the project start date.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Updates the project end date.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Budget getBudget() {
        return budget;
    }

    public List<Task> getTasks() {
        return List.copyOf(tasks);
    }

    public List<Resource> getResources() {
        return List.copyOf(resources);
    }

    public List<Milestone> getMilestones() {
        return List.copyOf(milestones);
    }

    public List<Dependency> getDependencies() {
        return List.copyOf(dependencies);
    }

    public List<Risk> getRisks() {
        return List.copyOf(risks);
    }
}
