package com.vertex.pm.model;

public class Resource {
    private final String id;
    private final String projectId;
    private String name;
    private String role;
    private boolean availability;
    private String skillSet;

    public Resource(String id, String projectId, String name, String role, boolean availability, String skillSet) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.role = role;
        this.availability = availability;
        this.skillSet = skillSet;
    }

    public String getId() { return id; }
    public String getProjectId() { return projectId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isAvailability() { return availability; }
    public void setAvailability(boolean availability) { this.availability = availability; }
    public String getSkillSet() { return skillSet; }
    public void setSkillSet(String skillSet) { this.skillSet = skillSet; }
}
