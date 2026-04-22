package com.vertex.pm.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Project {
    // Favor composition over inheritance: Project is composed with tasks, resources, milestones, expenses, risks, and team members.
    private final String id;
    private String name;
    private String description;
    private String managerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private String objectives;
    private int progressPercent;
    private Budget budget;
    private final List<Task> tasks = new ArrayList<>();
    private final List<Resource> resources = new ArrayList<>();
    private final List<TeamMember> teamMembers = new ArrayList<>();
    private final List<Milestone> milestones = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Risk> risks = new ArrayList<>();
    private final List<Dependency> dependencies = new ArrayList<>();

    public Project(String id, String name, String description, String managerName, LocalDate startDate,
                   LocalDate endDate, ProjectStatus status, String objectives, int progressPercent, Budget budget) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.managerName = managerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.objectives = objectives;
        this.progressPercent = progressPercent;
        this.budget = budget;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    public int getProgressPercent() { return progressPercent; }
    public void setProgressPercent(int progressPercent) { this.progressPercent = progressPercent; }
    public Budget getBudget() { return budget; }
    public void setBudget(Budget budget) { this.budget = budget; }
    public List<Task> getTasks() { return List.copyOf(tasks); }
    public void setTasks(List<Task> tasks) { this.tasks.clear(); this.tasks.addAll(tasks); }
    public List<Resource> getResources() { return List.copyOf(resources); }
    public void setResources(List<Resource> resources) { this.resources.clear(); this.resources.addAll(resources); }
    public List<TeamMember> getTeamMembers() { return List.copyOf(teamMembers); }
    public void setTeamMembers(List<TeamMember> teamMembers) { this.teamMembers.clear(); this.teamMembers.addAll(teamMembers); }
    public List<Milestone> getMilestones() { return List.copyOf(milestones); }
    public void setMilestones(List<Milestone> milestones) { this.milestones.clear(); this.milestones.addAll(milestones); }
    public List<Expense> getExpenses() { return List.copyOf(expenses); }
    public void setExpenses(List<Expense> expenses) { this.expenses.clear(); this.expenses.addAll(expenses); }
    public List<Risk> getRisks() { return List.copyOf(risks); }
    public void setRisks(List<Risk> risks) { this.risks.clear(); this.risks.addAll(risks); }
    public List<Dependency> getDependencies() { return List.copyOf(dependencies); }
    public void setDependencies(List<Dependency> dependencies) { this.dependencies.clear(); this.dependencies.addAll(dependencies); }
}
