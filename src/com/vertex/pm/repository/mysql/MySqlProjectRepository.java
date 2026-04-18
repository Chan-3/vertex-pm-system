package com.vertex.pm.repository.mysql;

import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.exception.RepositoryException;
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
import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles project persistence against a local MySQL database.
 * SRP: this class is only responsible for local database access.
 */
public class MySqlProjectRepository implements ProjectRepository {
    private final DatabaseConnectionManager connectionManager;

    /**
     * Creates the MySQL repository.
     */
    public MySqlProjectRepository(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<Project> getAllProjects() throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name, description, start_date, end_date, status FROM project ORDER BY id")) {
            List<Project> projects = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(loadProjectAggregate(connection, resultSet));
                }
            }
            return projects;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to read projects from MySQL.", e);
        }
    }

    @Override
    public Optional<Project> getProjectById(int projectId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, name, description, start_date, end_date, status FROM project WHERE id = ?")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(loadProjectAggregate(connection, resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to read project by id from MySQL.", e);
        }
    }

    @Override
    public void saveProject(Project project) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                insertProject(connection, project);
                replaceProjectChildren(connection, project);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RepositoryException("Failed to save project to MySQL.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save project to MySQL.", e);
        }
    }

    @Override
    public void updateProject(Project project) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                updateProjectRow(connection, project);
                replaceProjectChildren(connection, project);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RepositoryException("Failed to update project in MySQL.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update project in MySQL.", e);
        }
    }

    @Override
    public void deleteProject(int projectId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                deleteChildren(connection, projectId);
                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM project WHERE id = ?")) {
                    statement.setInt(1, projectId);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RepositoryException("Failed to delete project from MySQL.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete project from MySQL.", e);
        }
    }

    private Project loadProjectAggregate(Connection connection, ResultSet projectRow) throws SQLException {
        int projectId = projectRow.getInt("id");
        Budget budget = loadBudget(connection, projectId);
        Project project = new Project(
                projectId,
                projectRow.getString("name"),
                projectRow.getString("description"),
                projectRow.getDate("start_date").toLocalDate(),
                projectRow.getDate("end_date").toLocalDate(),
                ProjectStatus.valueOf(projectRow.getString("status")),
                budget
        );

        for (Task task : loadTasks(connection, projectId)) {
            project.addTask(task);
        }
        for (Resource resource : loadResources(connection, projectId)) {
            project.addResource(resource);
        }
        for (Milestone milestone : loadMilestones(connection, projectId)) {
            project.addMilestone(milestone);
        }
        for (Dependency dependency : loadDependencies(connection, projectId)) {
            project.addDependency(dependency);
        }
        for (Risk risk : loadRisks(connection, projectId)) {
            project.addRisk(risk);
        }
        return project;
    }

    private Budget loadBudget(Connection connection, int projectId) throws SQLException {
        double totalAmount = 0.0;
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT total_amount FROM budget WHERE project_id = ?")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    totalAmount = resultSet.getDouble("total_amount");
                }
            }
        }
        Budget budget = new Budget(projectId, totalAmount);
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, amount, category, date FROM expense WHERE project_id = ? ORDER BY id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    budget.addExpense(new Expense(
                            resultSet.getInt("id"),
                            resultSet.getInt("project_id"),
                            resultSet.getDouble("amount"),
                            resultSet.getString("category"),
                            resultSet.getDate("date").toLocalDate()
                    ));
                }
            }
        }
        return budget;
    }

    private List<Task> loadTasks(Connection connection, int projectId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, name, start_date, end_date, status FROM task WHERE project_id = ? ORDER BY id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new Task(
                            resultSet.getInt("id"),
                            resultSet.getInt("project_id"),
                            resultSet.getString("name"),
                            resultSet.getDate("start_date").toLocalDate(),
                            resultSet.getDate("end_date").toLocalDate(),
                            TaskStatus.valueOf(resultSet.getString("status"))
                    ));
                }
            }
        }
        return tasks;
    }

    private List<Resource> loadResources(Connection connection, int projectId) throws SQLException {
        List<Resource> resources = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, name, role, available FROM resource WHERE project_id = ? ORDER BY id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(new Resource(
                            resultSet.getInt("id"),
                            resultSet.getString("name"),
                            resultSet.getString("role"),
                            resultSet.getBoolean("available")
                    ));
                }
            }
        }
        return resources;
    }

    private List<Milestone> loadMilestones(Connection connection, int projectId) throws SQLException {
        List<Milestone> milestones = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, name, target_date, completed FROM milestone WHERE project_id = ? ORDER BY id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    milestones.add(new Milestone(
                            resultSet.getInt("id"),
                            resultSet.getInt("project_id"),
                            resultSet.getString("name"),
                            resultSet.getDate("target_date").toLocalDate(),
                            resultSet.getBoolean("completed")
                    ));
                }
            }
        }
        return milestones;
    }

    private List<Dependency> loadDependencies(Connection connection, int projectId) throws SQLException {
        List<Dependency> dependencies = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT d.task_id, d.depends_on_task_id " +
                        "FROM dependency d JOIN task t ON d.task_id = t.id WHERE t.project_id = ? ORDER BY d.task_id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    dependencies.add(new Dependency(
                            resultSet.getInt("task_id"),
                            resultSet.getInt("depends_on_task_id")
                    ));
                }
            }
        }
        return dependencies;
    }

    private List<Risk> loadRisks(Connection connection, int projectId) throws SQLException {
        List<Risk> risks = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, description, severity FROM risk WHERE project_id = ? ORDER BY id")) {
            statement.setInt(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    risks.add(new Risk(
                            resultSet.getInt("id"),
                            resultSet.getInt("project_id"),
                            resultSet.getString("description"),
                            RiskSeverity.valueOf(resultSet.getString("severity"))
                    ));
                }
            }
        }
        return risks;
    }

    private void insertProject(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO project (id, name, description, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setInt(1, project.getId());
            statement.setString(2, project.getName());
            statement.setString(3, project.getDescription());
            statement.setDate(4, Date.valueOf(project.getStartDate()));
            statement.setDate(5, Date.valueOf(project.getEndDate()));
            statement.setString(6, project.getStatus().name());
            statement.executeUpdate();
        }
    }

    private void updateProjectRow(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE project SET name = ?, description = ?, start_date = ?, end_date = ?, status = ? WHERE id = ?")) {
            statement.setString(1, project.getName());
            statement.setString(2, project.getDescription());
            statement.setDate(3, Date.valueOf(project.getStartDate()));
            statement.setDate(4, Date.valueOf(project.getEndDate()));
            statement.setString(5, project.getStatus().name());
            statement.setInt(6, project.getId());
            statement.executeUpdate();
        }
    }

    private void replaceProjectChildren(Connection connection, Project project) throws SQLException {
        deleteChildren(connection, project.getId());
        insertBudget(connection, project);
        insertTasks(connection, project);
        insertResources(connection, project);
        insertMilestones(connection, project);
        insertDependencies(connection, project);
        insertRisks(connection, project);
        insertExpenses(connection, project);
    }

    private void deleteChildren(Connection connection, int projectId) throws SQLException {
        executeDelete(connection, "DELETE FROM assignment WHERE task_id IN (SELECT id FROM task WHERE project_id = ?)", projectId);
        executeDelete(connection, "DELETE FROM dependency WHERE task_id IN (SELECT id FROM task WHERE project_id = ?) OR depends_on_task_id IN (SELECT id FROM task WHERE project_id = ?)", projectId, projectId);
        executeDelete(connection, "DELETE FROM expense WHERE project_id = ?", projectId);
        executeDelete(connection, "DELETE FROM budget WHERE project_id = ?", projectId);
        executeDelete(connection, "DELETE FROM milestone WHERE project_id = ?", projectId);
        executeDelete(connection, "DELETE FROM resource WHERE project_id = ?", projectId);
        executeDelete(connection, "DELETE FROM risk WHERE project_id = ?", projectId);
        executeDelete(connection, "DELETE FROM task WHERE project_id = ?", projectId);
    }

    private void insertBudget(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO budget (project_id, total_amount) VALUES (?, ?)")) {
            statement.setInt(1, project.getId());
            statement.setDouble(2, project.getBudget().getTotalAmount());
            statement.executeUpdate();
        }
    }

    private void insertTasks(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO task (id, project_id, name, start_date, end_date, status) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (Task task : project.getTasks()) {
                statement.setInt(1, task.getId());
                statement.setInt(2, task.getProjectId());
                statement.setString(3, task.getName());
                statement.setDate(4, Date.valueOf(task.getStartDate()));
                statement.setDate(5, Date.valueOf(task.getEndDate()));
                statement.setString(6, task.getStatus().name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertResources(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO resource (id, project_id, name, role, available) VALUES (?, ?, ?, ?, ?)")) {
            for (Resource resource : project.getResources()) {
                statement.setInt(1, resource.getId());
                statement.setInt(2, project.getId());
                statement.setString(3, resource.getName());
                statement.setString(4, resource.getRole());
                statement.setBoolean(5, resource.isAvailable());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertMilestones(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO milestone (id, project_id, name, target_date, completed) VALUES (?, ?, ?, ?, ?)")) {
            for (Milestone milestone : project.getMilestones()) {
                statement.setInt(1, milestone.getId());
                statement.setInt(2, milestone.getProjectId());
                statement.setString(3, milestone.getName());
                statement.setDate(4, Date.valueOf(milestone.getTargetDate()));
                statement.setBoolean(5, milestone.isCompleted());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertDependencies(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO dependency (task_id, depends_on_task_id) VALUES (?, ?)")) {
            for (Dependency dependency : project.getDependencies()) {
                statement.setInt(1, dependency.getTaskId());
                statement.setInt(2, dependency.getDependsOnTaskId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertRisks(Connection connection, Project project) throws SQLException {
        ensureRiskTable(connection);
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO risk (id, project_id, description, severity) VALUES (?, ?, ?, ?)")) {
            for (Risk risk : project.getRisks()) {
                statement.setInt(1, risk.getId());
                statement.setInt(2, risk.getProjectId());
                statement.setString(3, risk.getDescription());
                statement.setString(4, risk.getSeverity().name());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void insertExpenses(Connection connection, Project project) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO expense (id, project_id, amount, category, date) VALUES (?, ?, ?, ?, ?)")) {
            for (Expense expense : project.getBudget().getExpenses()) {
                statement.setInt(1, expense.getId());
                statement.setInt(2, expense.getProjectId());
                statement.setDouble(3, expense.getAmount());
                statement.setString(4, expense.getCategory());
                statement.setDate(5, Date.valueOf(expense.getDate()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void executeDelete(Connection connection, String sql, int... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int index = 0; index < values.length; index++) {
                statement.setInt(index + 1, values[index]);
            }
            statement.executeUpdate();
        }
    }

    private void ensureRiskTable(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS risk (
                        id INT PRIMARY KEY,
                        project_id INT,
                        description VARCHAR(255),
                        severity VARCHAR(20),
                        FOREIGN KEY (project_id) REFERENCES project(id)
                    )
                    """);
        }
    }
}
