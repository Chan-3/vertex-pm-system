package com.vertex.pm.model;

import java.time.LocalDate;

/**
 * Represents a task that belongs to a project.
 * GRASP Information Expert: the task exposes its own completion status logic.
 */
public class Task {
    private final int id;
    private final int projectId;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private TaskStatus status;

    /**
     * Creates a task.
     */
    public Task(int id, int projectId, String name, LocalDate startDate, LocalDate endDate, TaskStatus status) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    /**
     * Returns a completion ratio used by project summaries.
     */
    public double getCompletionRatio() {
        return switch (status) {
            case NOT_STARTED -> 0.0;
            case IN_PROGRESS -> 0.5;
            case COMPLETED -> 1.0;
        };
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getName() {
        return name;
    }

    /**
     * Updates the task name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * Updates the task start date.
     */
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    /**
     * Updates the task end date.
     */
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}
