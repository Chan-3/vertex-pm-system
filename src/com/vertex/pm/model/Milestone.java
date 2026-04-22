package com.vertex.pm.model;

import java.time.LocalDate;

public class Milestone {
    private final String id;
    private final String projectId;
    private String name;
    private LocalDate targetDate;
    private boolean completionStatus;
    private String description;

    public Milestone(String id, String projectId, String name, LocalDate targetDate, boolean completionStatus, String description) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.targetDate = targetDate;
        this.completionStatus = completionStatus;
        this.description = description;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public boolean isCompletionStatus() { return completionStatus; }
    public void setCompletionStatus(boolean completionStatus) { this.completionStatus = completionStatus; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
