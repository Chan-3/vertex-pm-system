package com.vertex.pm.model;

import java.time.LocalDate;

/**
 * Represents a project milestone.
 */
public class Milestone {
    private final int id;
    private final int projectId;
    private String name;
    private LocalDate targetDate;
    private boolean completed;

    /**
     * Creates a milestone.
     */
    public Milestone(int id, int projectId, String name, LocalDate targetDate, boolean completed) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.targetDate = targetDate;
        this.completed = completed;
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
     * Updates the milestone name.
     */
    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    /**
     * Updates the milestone target date.
     */
    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * Updates milestone completion status.
     */
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
