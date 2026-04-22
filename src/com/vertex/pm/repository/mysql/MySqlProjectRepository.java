package com.vertex.pm.repository.mysql;

import com.vertex.pm.exception.ExceptionType;
import com.vertex.pm.exception.ProjectManagementException;
import com.vertex.pm.exception.RepositoryException;
import com.vertex.pm.model.Budget;
import com.vertex.pm.model.Dependency;
import com.vertex.pm.model.Expense;
import com.vertex.pm.model.Milestone;
import com.vertex.pm.model.Project;
import com.vertex.pm.model.ProjectReport;
import com.vertex.pm.model.ProjectStatus;
import com.vertex.pm.model.Resource;
import com.vertex.pm.model.Risk;
import com.vertex.pm.model.RiskSeverity;
import com.vertex.pm.model.Task;
import com.vertex.pm.model.TaskStatus;
import com.vertex.pm.model.TeamMember;
import com.vertex.pm.repository.ProjectRepository;
import com.vertex.pm.util.DatabaseConnectionManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class MySqlProjectRepository implements ProjectRepository {
    // Repository Pattern + Pure Fabrication: SQL access is isolated here instead of being mixed into services/models.
    private final DatabaseConnectionManager connectionManager;

    public MySqlProjectRepository() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }

    @Override
    public List<Project> getAllProjects() throws ProjectManagementException {
        String sql = """
                SELECT id, name, description, manager_name, start_date, end_date, status, objectives, progress_pct
                FROM project
                ORDER BY start_date
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Project> projects = new ArrayList<>();
            while (resultSet.next()) {
                projects.add(mapProject(connection, resultSet));
            }
            return projects;
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load projects from pm_db.", exception);
        }
    }

    @Override
    public Optional<Project> getProjectById(String projectId) throws ProjectManagementException {
        String sql = """
                SELECT id, name, description, manager_name, start_date, end_date, status, objectives, progress_pct
                FROM project
                WHERE id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapProject(connection, resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load project " + projectId + ".", exception);
        }
    }

    @Override
    public Project createProject(Project project) throws ProjectManagementException {
        String projectSql = """
                INSERT INTO project (
                    id, name, description, manager_name, start_date, end_date,
                    status, objectives, progress_pct, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        String budgetSql = """
                INSERT INTO budget (
                    budget_id, project_id, planned_amount, actual_cost, variance, updated_at
                ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = connectionManager.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement projectStatement = connection.prepareStatement(projectSql);
                 PreparedStatement budgetStatement = connection.prepareStatement(budgetSql)) {
                projectStatement.setString(1, project.getId());
                projectStatement.setString(2, project.getName());
                projectStatement.setString(3, project.getDescription());
                projectStatement.setString(4, project.getManagerName());
                projectStatement.setDate(5, Date.valueOf(project.getStartDate()));
                projectStatement.setDate(6, Date.valueOf(project.getEndDate()));
                projectStatement.setString(7, project.getStatus().name());
                projectStatement.setString(8, project.getObjectives());
                projectStatement.setInt(9, project.getProgressPercent());
                projectStatement.executeUpdate();

                budgetStatement.setString(1, "BGT-" + project.getId());
                budgetStatement.setString(2, project.getId());
                budgetStatement.setDouble(3, project.getBudget().getTotalAmount());
                budgetStatement.setDouble(4, project.getBudget().getSpentAmount());
                budgetStatement.setDouble(5, project.getBudget().getRemainingAmount());
                budgetStatement.executeUpdate();

                connection.commit();
                return getProjectById(project.getId()).orElse(project);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to create project.", exception);
        }
    }

    @Override
    public Project patchProject(String projectId, Map<String, Object> changes) throws ProjectManagementException {
        Map<String, String> projectFields = Map.of(
                "name", "name",
                "description", "description",
                "managerName", "manager_name",
                "startDate", "start_date",
                "endDate", "end_date",
                "status", "status",
                "objectives", "objectives",
                "progressPercent", "progress_pct"
        );
        Map<String, String> budgetFields = Map.of(
                "budgetTotal", "planned_amount",
                "budgetSpent", "actual_cost"
        );
        runPatch("project", "id", projectId, changes, projectFields, ExceptionType.PROJECT_NOT_FOUND, "Project");
        runPatch("budget", "project_id", projectId, changes, budgetFields, ExceptionType.BUDGET_NOT_FOUND, "Budget");
        refreshBudgetVariance(projectId);
        return getProjectById(projectId).orElseThrow(() -> new ProjectManagementException(
                ExceptionType.PROJECT_NOT_FOUND, "Project not found: " + projectId, 404));
    }

    @Override
    public Project patchProjectProgress(String projectId, Map<String, Object> changes) throws ProjectManagementException {
        Map<String, String> allowedFields = Map.of(
                "progressPercent", "progress_pct",
                "status", "status"
        );
        runPatch("project", "id", projectId, changes, allowedFields, ExceptionType.PROJECT_NOT_FOUND, "Project");
        return getProjectById(projectId).orElseThrow(() -> new ProjectManagementException(
                ExceptionType.PROJECT_NOT_FOUND, "Project not found: " + projectId, 404));
    }

    @Override
    public void deleteProject(String projectId) throws ProjectManagementException {
        deleteById("project", "id", projectId, ExceptionType.PROJECT_NOT_FOUND, "Project");
    }

    @Override
    public List<Task> getAllTasks() throws ProjectManagementException {
        return queryTasks(null);
    }

    @Override
    public Task createTask(Task task) throws ProjectManagementException {
        String sql = """
                INSERT INTO task (
                    id, project_id, task_name, description, start_date, due_date,
                    status, priority, assigned_to, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, task.getId());
            statement.setString(2, task.getProjectId());
            statement.setString(3, task.getName());
            statement.setString(4, task.getDescription());
            statement.setDate(5, Date.valueOf(task.getStartDate()));
            statement.setDate(6, Date.valueOf(task.getDueDate()));
            statement.setString(7, task.getStatus().name());
            statement.setString(8, task.getPriority());
            statement.setString(9, task.getAssignedTo());
            statement.executeUpdate();
            return getTaskById(task.getId());
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to create task.", exception);
        }
    }

    @Override
    public Task patchTask(String taskId, Map<String, Object> changes) throws ProjectManagementException {
        Map<String, String> allowedFields = Map.of(
                "name", "task_name",
                "description", "description",
                "startDate", "start_date",
                "dueDate", "due_date",
                "status", "status",
                "priority", "priority",
                "assignedTo", "assigned_to"
        );
        runPatch("task", "id", taskId, changes, allowedFields, ExceptionType.TASK_NOT_FOUND, "Task");
        return getTaskById(taskId);
    }

    @Override
    public void deleteTask(String taskId) throws ProjectManagementException {
        deleteById("task", "id", taskId, ExceptionType.TASK_NOT_FOUND, "Task");
    }

    @Override
    public List<Resource> getAllResources() throws ProjectManagementException {
        String sql = """
                SELECT id, project_id, name, role, availability, skill_set
                FROM resource
                ORDER BY name
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Resource> resources = new ArrayList<>();
            while (resultSet.next()) {
                resources.add(mapResource(resultSet));
            }
            return resources;
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load resources.", exception);
        }
    }

    @Override
    public Resource createResource(Resource resource) throws ProjectManagementException {
        String sql = """
                INSERT INTO resource (id, project_id, name, role, availability, skill_set, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resource.getId());
            statement.setString(2, resource.getProjectId());
            statement.setString(3, resource.getName());
            statement.setString(4, resource.getRole());
            statement.setBoolean(5, resource.isAvailability());
            statement.setString(6, resource.getSkillSet());
            statement.executeUpdate();
            return getResourceById(resource.getId());
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to create resource.", exception);
        }
    }

    @Override
    public Resource patchResource(String resourceId, Map<String, Object> changes) throws ProjectManagementException {
        Map<String, String> allowedFields = Map.of(
                "name", "name",
                "role", "role",
                "availability", "availability",
                "skillSet", "skill_set"
        );
        runPatch("resource", "id", resourceId, changes, allowedFields, ExceptionType.RESOURCE_NOT_FOUND, "Resource");
        return getResourceById(resourceId);
    }

    @Override
    public void deleteResource(String resourceId) throws ProjectManagementException {
        deleteById("resource", "id", resourceId, ExceptionType.RESOURCE_NOT_FOUND, "Resource");
    }

    @Override
    public List<Milestone> getAllMilestones() throws ProjectManagementException {
        String sql = """
                SELECT id, project_id, name, target_date, completion_status, description
                FROM milestone
                ORDER BY target_date
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Milestone> milestones = new ArrayList<>();
            while (resultSet.next()) {
                milestones.add(mapMilestone(resultSet));
            }
            return milestones;
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load milestones.", exception);
        }
    }

    @Override
    public Milestone createMilestone(Milestone milestone) throws ProjectManagementException {
        String sql = """
                INSERT INTO milestone (id, project_id, name, target_date, completion_status, description, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, milestone.getId());
            statement.setString(2, milestone.getProjectId());
            statement.setString(3, milestone.getName());
            statement.setDate(4, Date.valueOf(milestone.getTargetDate()));
            statement.setBoolean(5, milestone.isCompletionStatus());
            statement.setString(6, milestone.getDescription());
            statement.executeUpdate();
            return getMilestoneById(milestone.getId());
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to create milestone.", exception);
        }
    }

    @Override
    public Milestone patchMilestone(String milestoneId, Map<String, Object> changes) throws ProjectManagementException {
        Map<String, String> allowedFields = Map.of(
                "name", "name",
                "targetDate", "target_date",
                "completionStatus", "completion_status",
                "description", "description"
        );
        runPatch("milestone", "id", milestoneId, changes, allowedFields, ExceptionType.MILESTONE_NOT_FOUND, "Milestone");
        return getMilestoneById(milestoneId);
    }

    @Override
    public void deleteMilestone(String milestoneId) throws ProjectManagementException {
        deleteById("milestone", "id", milestoneId, ExceptionType.MILESTONE_NOT_FOUND, "Milestone");
    }

    @Override
    public List<Expense> getAllExpenses() throws ProjectManagementException {
        String sql = """
                SELECT id, project_id, expense_date, description, category, amount
                FROM expense
                ORDER BY expense_date
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            List<Expense> expenses = new ArrayList<>();
            while (resultSet.next()) {
                expenses.add(mapExpense(resultSet));
            }
            return expenses;
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load expenses.", exception);
        }
    }

    @Override
    public Expense createExpense(Expense expense) throws ProjectManagementException {
        String sql = """
                INSERT INTO expense (id, project_id, expense_date, description, category, amount, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, expense.getId());
            statement.setString(2, expense.getProjectId());
            statement.setDate(3, Date.valueOf(expense.getExpenseDate()));
            statement.setString(4, expense.getDescription());
            statement.setString(5, expense.getCategory());
            statement.setDouble(6, expense.getAmount());
            statement.executeUpdate();
            refreshBudgetActualCost(connection, expense.getProjectId());
            return getExpenseById(expense.getId());
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to create expense.", exception);
        }
    }

    @Override
    public Expense patchExpense(String expenseId, Map<String, Object> changes) throws ProjectManagementException {
        Expense existing = getExpenseById(expenseId);
        Map<String, String> allowedFields = Map.of(
                "expenseDate", "expense_date",
                "description", "description",
                "category", "category",
                "amount", "amount"
        );
        runPatch("expense", "id", expenseId, changes, allowedFields, ExceptionType.EXPENSE_NOT_FOUND, "Expense");
        refreshBudgetVariance(existing.getProjectId());
        return getExpenseById(expenseId);
    }

    @Override
    public void deleteExpense(String expenseId) throws ProjectManagementException {
        Expense existing = getExpenseById(expenseId);
        deleteById("expense", "id", expenseId, ExceptionType.EXPENSE_NOT_FOUND, "Expense");
        refreshBudgetVariance(existing.getProjectId());
    }

    @Override
    public ProjectReport getProjectReport(String projectId) throws ProjectManagementException {
        Project project = getProjectById(projectId).orElseThrow(() -> new ProjectManagementException(
                ExceptionType.PROJECT_NOT_FOUND, "Project not found: " + projectId, 404));
        List<Task> tasks = project.getTasks();
        int completed = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
        int inProgress = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS).count();
        int planned = (int) tasks.stream().filter(task -> task.getStatus() == TaskStatus.PLANNED).count();
        int overdue = (int) tasks.stream()
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED)
                .count();
        List<Map<String, Object>> milestones = project.getMilestones().stream()
                .map(milestone -> Map.<String, Object>of(
                        "id", milestone.getId(),
                        "name", milestone.getName(),
                        "targetDate", milestone.getTargetDate().toString(),
                        "completionStatus", milestone.isCompletionStatus()
                ))
                .collect(Collectors.toList());
        List<Map<String, Object>> taskDetails = tasks.stream()
                .map(task -> Map.<String, Object>of(
                        "id", task.getId(),
                        "taskName", task.getName(),
                        "description", task.getDescription(),
                        "priority", task.getPriority(),
                        "assignedTo", task.getAssignedTo(),
                        "status", task.getStatus().name(),
                        "startDate", task.getStartDate().toString(),
                        "dueDate", task.getDueDate().toString()
                ))
                .collect(Collectors.toList());
        return new ProjectReport(
                project.getId(),
                project.getName(),
                tasks.size(),
                completed,
                inProgress,
                planned,
                overdue,
                project.getProgressPercent(),
                project.getBudget().getTotalAmount(),
                project.getBudget().getSpentAmount(),
                project.getBudget().getRemainingAmount(),
                milestones,
                taskDetails
        );
    }

    private Project mapProject(Connection connection, ResultSet resultSet) throws SQLException {
        String projectId = resultSet.getString("id");
        Project project = new Project(
                projectId,
                resultSet.getString("name"),
                resultSet.getString("description"),
                resultSet.getString("manager_name"),
                resultSet.getDate("start_date").toLocalDate(),
                resultSet.getDate("end_date").toLocalDate(),
                ProjectStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("objectives"),
                resultSet.getInt("progress_pct"),
                queryBudget(connection, projectId)
        );
        project.setTasks(queryTasks(connection, projectId));
        project.setResources(queryResources(connection, projectId));
        project.setTeamMembers(queryTeamMembers(connection, projectId));
        project.setMilestones(queryMilestones(connection, projectId));
        project.setExpenses(queryExpenses(connection, projectId));
        project.setRisks(queryRisks(connection, projectId));
        project.setDependencies(queryDependencies(connection, projectId));
        return project;
    }

    private Budget queryBudget(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT planned_amount, actual_cost FROM budget WHERE project_id = ?")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Budget(projectId, resultSet.getDouble("planned_amount"), resultSet.getDouble("actual_cost"));
                }
            }
        }
        return new Budget(projectId, 0, 0);
    }

    private List<Task> queryTasks(String projectId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection()) {
            return queryTasks(connection, projectId);
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load tasks.", exception);
        }
    }

    private List<Task> queryTasks(Connection connection, String projectId) throws SQLException {
        String sql = """
                SELECT id, project_id, task_name, description, start_date, due_date, status, priority, assigned_to
                FROM task
                %s
                ORDER BY start_date
                """.formatted(projectId == null ? "" : "WHERE project_id = ?");
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            if (projectId != null) {
                statement.setString(1, projectId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Task> tasks = new ArrayList<>();
                while (resultSet.next()) {
                    tasks.add(mapTask(resultSet));
                }
                return tasks;
            }
        }
    }

    private List<Resource> queryResources(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, name, role, availability, skill_set FROM resource WHERE project_id = ? ORDER BY name")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Resource> resources = new ArrayList<>();
                while (resultSet.next()) {
                    resources.add(mapResource(resultSet));
                }
                return resources;
            }
        }
    }

    private List<TeamMember> queryTeamMembers(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT member_id, member_name, member_role, email FROM project_team_member WHERE project_id = ? ORDER BY member_name")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<TeamMember> teamMembers = new ArrayList<>();
                while (resultSet.next()) {
                    teamMembers.add(new TeamMember(
                            resultSet.getString("member_id"),
                            resultSet.getString("member_name"),
                            resultSet.getString("member_role"),
                            resultSet.getString("email")
                    ));
                }
                return teamMembers;
            }
        }
    }

    private List<Milestone> queryMilestones(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, name, target_date, completion_status, description FROM milestone WHERE project_id = ? ORDER BY target_date")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Milestone> milestones = new ArrayList<>();
                while (resultSet.next()) {
                    milestones.add(mapMilestone(resultSet));
                }
                return milestones;
            }
        }
    }

    private List<Expense> queryExpenses(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, expense_date, description, category, amount FROM expense WHERE project_id = ? ORDER BY expense_date")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Expense> expenses = new ArrayList<>();
                while (resultSet.next()) {
                    expenses.add(mapExpense(resultSet));
                }
                return expenses;
            }
        }
    }

    private List<Risk> queryRisks(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id, project_id, description, severity, status, mitigation_plan FROM risk WHERE project_id = ? ORDER BY severity DESC")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Risk> risks = new ArrayList<>();
                while (resultSet.next()) {
                    risks.add(new Risk(
                            resultSet.getString("id"),
                            resultSet.getString("project_id"),
                            resultSet.getString("description"),
                            RiskSeverity.valueOf(resultSet.getString("severity")),
                            resultSet.getString("status"),
                            resultSet.getString("mitigation_plan")
                    ));
                }
                return risks;
            }
        }
    }

    private List<Dependency> queryDependencies(Connection connection, String projectId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT task_id, depends_on_task_id FROM dependency WHERE project_id = ? ORDER BY task_id")) {
            statement.setString(1, projectId);
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Dependency> dependencies = new ArrayList<>();
                while (resultSet.next()) {
                    dependencies.add(new Dependency(
                            resultSet.getString("task_id"),
                            resultSet.getString("depends_on_task_id")
                    ));
                }
                return dependencies;
            }
        }
    }

    private Task getTaskById(String taskId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, project_id, task_name, description, start_date, due_date, status, priority, assigned_to FROM task WHERE id = ?")) {
            statement.setString(1, taskId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapTask(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load task " + taskId + ".", exception);
        }
        throw new ProjectManagementException(ExceptionType.TASK_NOT_FOUND, "Task not found: " + taskId, 404);
    }

    private Resource getResourceById(String resourceId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, project_id, name, role, availability, skill_set FROM resource WHERE id = ?")) {
            statement.setString(1, resourceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResource(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load resource " + resourceId + ".", exception);
        }
        throw new ProjectManagementException(ExceptionType.RESOURCE_NOT_FOUND, "Resource not found: " + resourceId, 404);
    }

    private Milestone getMilestoneById(String milestoneId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, project_id, name, target_date, completion_status, description FROM milestone WHERE id = ?")) {
            statement.setString(1, milestoneId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapMilestone(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load milestone " + milestoneId + ".", exception);
        }
        throw new ProjectManagementException(ExceptionType.MILESTONE_NOT_FOUND, "Milestone not found: " + milestoneId, 404);
    }

    private Expense getExpenseById(String expenseId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, project_id, expense_date, description, category, amount FROM expense WHERE id = ?")) {
            statement.setString(1, expenseId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapExpense(resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to load expense " + expenseId + ".", exception);
        }
        throw new ProjectManagementException(ExceptionType.EXPENSE_NOT_FOUND, "Expense not found: " + expenseId, 404);
    }

    private Task mapTask(ResultSet resultSet) throws SQLException {
        return new Task(
                resultSet.getString("id"),
                resultSet.getString("project_id"),
                resultSet.getString("task_name"),
                resultSet.getString("description"),
                resultSet.getDate("start_date").toLocalDate(),
                resultSet.getDate("due_date").toLocalDate(),
                TaskStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("priority"),
                resultSet.getString("assigned_to")
        );
    }

    private Resource mapResource(ResultSet resultSet) throws SQLException {
        return new Resource(
                resultSet.getString("id"),
                resultSet.getString("project_id"),
                resultSet.getString("name"),
                resultSet.getString("role"),
                resultSet.getBoolean("availability"),
                resultSet.getString("skill_set")
        );
    }

    private Milestone mapMilestone(ResultSet resultSet) throws SQLException {
        return new Milestone(
                resultSet.getString("id"),
                resultSet.getString("project_id"),
                resultSet.getString("name"),
                resultSet.getDate("target_date").toLocalDate(),
                resultSet.getBoolean("completion_status"),
                resultSet.getString("description")
        );
    }

    private Expense mapExpense(ResultSet resultSet) throws SQLException {
        return new Expense(
                resultSet.getString("id"),
                resultSet.getString("project_id"),
                resultSet.getDate("expense_date").toLocalDate(),
                resultSet.getString("description"),
                resultSet.getString("category"),
                resultSet.getDouble("amount")
        );
    }

    private void runPatch(String tableName, String idColumn, String idValue, Map<String, Object> changes,
                          Map<String, String> allowedFields, ExceptionType missingType, String label) throws ProjectManagementException {
        // Open/Closed support: PATCH behavior is extended through field maps without changing controller flow.
        Map<String, Object> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            if (entry.getValue() != null && allowedFields.containsKey(entry.getKey())) {
                filtered.put(allowedFields.get(entry.getKey()), entry.getValue());
            }
        }
        if (filtered.isEmpty()) {
            return;
        }
        String assignments = filtered.keySet().stream()
                .map(column -> column + " = ?")
                .collect(Collectors.joining(", "));
        String sql = "UPDATE " + tableName + " SET " + assignments + ", updated_at = CURRENT_TIMESTAMP WHERE " + idColumn + " = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (Map.Entry<String, Object> entry : filtered.entrySet()) {
                setValue(statement, index++, entry.getKey(), entry.getValue());
            }
            statement.setString(index, idValue);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new ProjectManagementException(missingType, label + " not found: " + idValue, 404);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to patch " + tableName + ".", exception);
        }
    }

    private void setValue(PreparedStatement statement, int index, String column, Object value) throws SQLException {
        if (column.endsWith("_date")) {
            statement.setDate(index, Date.valueOf(LocalDate.parse(String.valueOf(value))));
            return;
        }
        if (value instanceof Boolean bool) {
            statement.setBoolean(index, bool);
            return;
        }
        if (value instanceof Number number) {
            if (value instanceof Integer) {
                statement.setInt(index, number.intValue());
            } else {
                statement.setDouble(index, number.doubleValue());
            }
            return;
        }
        statement.setString(index, String.valueOf(value));
    }

    private void deleteById(String tableName, String idColumn, String idValue, ExceptionType missingType, String label)
            throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + idColumn + " = ?")) {
            statement.setString(1, idValue);
            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new ProjectManagementException(missingType, label + " not found: " + idValue, 404);
            }
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to delete from " + tableName + ".", exception);
        }
    }

    private void refreshBudgetVariance(String projectId) throws ProjectManagementException {
        try (Connection connection = connectionManager.getConnection()) {
            refreshBudgetActualCost(connection, projectId);
        } catch (SQLException exception) {
            throw new RepositoryException("Unable to refresh budget totals.", exception);
        }
    }

    private void refreshBudgetActualCost(Connection connection, String projectId) throws SQLException {
        double totalExpenses = 0;
        try (PreparedStatement totalStatement = connection.prepareStatement(
                "SELECT COALESCE(SUM(amount), 0) AS total_amount FROM expense WHERE project_id = ?")) {
            totalStatement.setString(1, projectId);
            try (ResultSet resultSet = totalStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalExpenses = resultSet.getDouble("total_amount");
                }
            }
        }

        try (PreparedStatement updateStatement = connection.prepareStatement(
                "UPDATE budget SET actual_cost = ?, variance = planned_amount - ? WHERE project_id = ?")) {
            updateStatement.setDouble(1, totalExpenses);
            updateStatement.setDouble(2, totalExpenses);
            updateStatement.setString(3, projectId);
            updateStatement.executeUpdate();
        }
    }
}
