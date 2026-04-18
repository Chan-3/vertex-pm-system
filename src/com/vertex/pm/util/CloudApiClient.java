package com.vertex.pm.util;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.model.Budget;
import com.vertex.pm.model.Dependency;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectStatus;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Risk;
import com.vertex.pm.model.RiskSeverity;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simulates a cloud API data source used as failover.
 * Strategy pattern: this provides the alternate remote data source behavior.
 */
public class CloudApiClient {
    private final List<Project> projects = new ArrayList<>();

    /**
     * Creates the client with seeded fallback data.
     */
    public CloudApiClient() {
        seedProjects();
    }

    /**
     * Returns all remote projects.
     */
    public List<Project> fetchProjects() throws ProjectManagementException {
        return List.copyOf(projects);
    }

    /**
     * Saves a project into the mock cloud source.
     */
    public void saveProject(Project project) throws ProjectManagementException {
        projects.add(project);
    }

    /**
     * Updates an existing project in the mock cloud source.
     */
    public void updateProject(Project project) throws ProjectManagementException {
        deleteProject(project.getId());
        projects.add(project);
    }

    /**
     * Deletes a project in the mock cloud source.
     */
    public void deleteProject(int projectId) throws ProjectManagementException {
        projects.removeIf(project -> project.getId() == projectId);
    }

    /**
     * Returns a project for mutation-oriented operations.
     */
    public Optional<Project> findProject(int projectId) {
        return projects.stream().filter(project -> project.getId() == projectId).findFirst();
    }

    /**
     * Seeds API fallback data.
     */
    private void seedProjects() {
        Budget alphaBudget = new Budget(1, 180000.00);
        alphaBudget.addExpense(new Expense(1, 1, 32000.00, "Design", LocalDate.now().minusDays(20)));
        alphaBudget.addExpense(new Expense(2, 1, 58000.00, "Development", LocalDate.now().minusDays(9)));

        Project alpha = new Project(
                1,
                "Vertex PM Dashboard",
                "Web-based project management subsystem with repository failover and risk tracking.",
                LocalDate.now().minusDays(30),
                LocalDate.now().plusDays(40),
                ProjectStatus.IN_PROGRESS,
                alphaBudget
        );
        alpha.addTask(new Task(101, 1, "Build controller endpoints", LocalDate.now().minusDays(14),
                LocalDate.now().plusDays(2), TaskStatus.IN_PROGRESS));
        alpha.addTask(new Task(102, 1, "Create UI dashboard", LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(6), TaskStatus.IN_PROGRESS));
        alpha.addTask(new Task(103, 1, "Add testing checklist", LocalDate.now().minusDays(4),
                LocalDate.now().plusDays(8), TaskStatus.NOT_STARTED));
        alpha.addResource(new Resource(501, "Asha", "Backend Developer", true));
        alpha.addResource(new Resource(502, "Kiran", "Frontend Developer", true));
        alpha.addMilestone(new Milestone(201, 1, "Layered architecture review", LocalDate.now().plusDays(3), false));
        alpha.addMilestone(new Milestone(202, 1, "UI delivery", LocalDate.now().plusDays(12), false));
        alpha.addDependency(new Dependency(103, 102));
        alpha.addRisk(new Risk(301, 1, "Database unavailable during demo", RiskSeverity.HIGH));
        alpha.addRisk(new Risk(302, 1, "Late UI refinements", RiskSeverity.MEDIUM));

        Budget betaBudget = new Budget(2, 95000.00);
        betaBudget.addExpense(new Expense(3, 2, 12000.00, "Analysis", LocalDate.now().minusDays(12)));
        betaBudget.addExpense(new Expense(4, 2, 15000.00, "Infrastructure", LocalDate.now().minusDays(5)));

        Project beta = new Project(
                2,
                "ERP Resource Upgrade",
                "Modernization of resource allocation flow with milestone and budget visibility.",
                LocalDate.now().minusDays(18),
                LocalDate.now().plusDays(55),
                ProjectStatus.PLANNED,
                betaBudget
        );
        beta.addTask(new Task(104, 2, "Finalize schema", LocalDate.now().minusDays(3),
                LocalDate.now().plusDays(5), TaskStatus.IN_PROGRESS));
        beta.addTask(new Task(105, 2, "Implement cloud fallback", LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(15), TaskStatus.NOT_STARTED));
        beta.addResource(new Resource(503, "Meera", "Business Analyst", true));
        beta.addMilestone(new Milestone(203, 2, "Schema sign-off", LocalDate.now().plusDays(7), false));
        beta.addRisk(new Risk(303, 2, "Requirement changes from stakeholders", RiskSeverity.MEDIUM));

        projects.add(alpha);
        projects.add(beta);
    }
}
