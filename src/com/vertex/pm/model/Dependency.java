package com.vertex.pm.model;

public class Dependency {
    private final String taskId;
    private final String dependsOnTaskId;

    public Dependency(String taskId, String dependsOnTaskId) {
        this.taskId = taskId;
        this.dependsOnTaskId = dependsOnTaskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getDependsOnTaskId() {
        return dependsOnTaskId;
    }
}
