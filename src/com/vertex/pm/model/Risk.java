package com.vertex.pm.model;

/**
 * Represents a project risk entry.
 */
public class Risk {
    private final int id;
    private final int projectId;
    private final String description;
    private final RiskSeverity severity;

    /**
     * Creates a risk.
     */
    public Risk(int id, int projectId, String description, RiskSeverity severity) {
        this.id = id;
        this.projectId = projectId;
        this.description = description;
        this.severity = severity;
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public String getDescription() {
        return description;
    }

    public RiskSeverity getSeverity() {
        return severity;
    }
}
