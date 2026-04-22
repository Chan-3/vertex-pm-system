package com.vertex.pm.model;

public class Risk {
    private final String id;
    private final String projectId;
    private final String description;
    private final RiskSeverity severity;
    private final String status;
    private final String mitigationPlan;

    public Risk(String id, String projectId, String description, RiskSeverity severity, String status, String mitigationPlan) {
        this.id = id;
        this.projectId = projectId;
        this.description = description;
        this.severity = severity;
        this.status = status;
        this.mitigationPlan = mitigationPlan;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getDescription() { return description; }
    public RiskSeverity getSeverity() { return severity; }
    public String getStatus() { return status; }
    public String getMitigationPlan() { return mitigationPlan; }
}
