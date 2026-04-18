package com.vertex.pm.model;

/**
 * Represents a task dependency relationship.
 */
public class Dependency {
    private final int taskId;
    private final int dependsOnTaskId;

    /**
     * Creates a dependency between two tasks.
     */
    public Dependency(int taskId, int dependsOnTaskId) {
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getDependsOnTaskId() {
        return dependsOnTaskId;
    }
}
