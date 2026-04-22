# Vertex PM System

Vertex PM System is a Java web-based Project Management System for a car manufacturing domain. It runs directly on `pm_db` and focuses on realistic project delivery workflows such as design freeze, procurement, assembly, testing, and launch readiness.

## What The System Covers

- Project Management
- Task Management
- Resource Management
- Milestone Management
- Dependency Tracking
- Budget And Expense Monitoring
- Risk Monitoring
- Project Reports

The seeded projects are:

- `Electric Vehicle Production Line Setup`
- `Engine Assembly Optimization`
- `Autonomous Driving Module Integration`

Each project includes:

- 10 realistic tasks
- assigned resources
- named team members
- milestone checkpoints
- expense records
- risk records
- task dependencies
- budget tracking

## Main Files And Folders

- `src/com/vertex/pm/Main.java`
  Starts the HTTP server and serves the UI.
- `src/com/vertex/pm/controller/ProjectController.java`
  Handles project routes such as create, patch, delete, progress update, and report generation.
- `src/com/vertex/pm/controller/SystemController.java`
  Handles task, resource, milestone, expense, dashboard, and monitoring routes.
- `src/com/vertex/pm/service/ProjectService.java`
  Applies business rules and validation before repository access.
- `src/com/vertex/pm/repository/ProjectRepository.java`
  Defines the repository contract used by the service layer.
- `src/com/vertex/pm/repository/mysql/MySqlProjectRepository.java`
  Implements direct `pm_db` access for projects, tasks, resources, milestones, expenses, budgets, team members, and dependencies.
- `src/com/vertex/pm/model/`
  Contains domain classes such as `Project`, `Task`, `Resource`, `Milestone`, `Expense`, `Risk`, `Budget`, `TeamMember`, `Dependency`, `ProjectMonitor`, and `ProjectReport`.
- `src/com/vertex/pm/exception/`
  Contains the structured exception system used across controller, service, and repository layers.
- `src/com/vertex/pm/util/DatabaseConnectionManager.java`
  Builds the JDBC connection using `.env`.
- `src/com/vertex/pm/util/EnvConfig.java`
  Loads environment values from `.env`.
- `src/com/vertex/pm/util/JsonUtil.java`
  Handles simple request parsing and JSON responses.
- `src/com/vertex/pm/util/IdGenerator.java`
  Creates readable string IDs for newly created records.
- `ui/index.html`
  Main user interface layout.
- `ui/app.js`
  Handles fetch calls, edit modals, partial update payloads, per-screen filtering, and report rendering.
- `ui/style.css`
  Styles the interface.
- `sql/schema.sql`
  Creates the full schema and inserts realistic sample data.
- `ui-create-samples.txt`
  Contains ready-to-copy sample values for the create forms in the UI.
- `test-exceptions.ps1`
  Runs quick exception-handling checks against the live API.
- `lib/mysql-connector-j-9.3.0.jar`
  MySQL JDBC driver used during compile and run.

## Database Design

The main tables in `pm_db` are:

- `project`
- `budget`
- `task`
- `resource`
- `project_team_member`
- `milestone`
- `expense`
- `risk`
- `dependency`

Important columns used by the application include:

- `project.manager_name`
- `project.objectives`
- `project.progress_pct`
- `budget.planned_amount`
- `budget.actual_cost`
- `budget.variance`
- `task.task_name`
- `task.due_date`
- `task.assigned_to`
- `resource.availability`
- `milestone.completion_status`
- `expense.expense_date`
- `risk.mitigation_plan`
- `project_team_member.member_name`
- `dependency.depends_on_task_id`

## Exception Handling

The system uses structured exception types so the UI gets meaningful error responses instead of silent failures. Important handled cases include:

- `PROJECT_NOT_FOUND`
- `DUPLICATE_PROJECT_NAME`
- `INVALID_PROJECT_DATES`
- `PROJECT_UPDATE_FAILED`
- `TASK_NOT_FOUND`
- `INVALID_TASK_DATES`
- `TASK_ALREADY_COMPLETED`
- `TASK_STATUS_INVALID`
- `RESOURCE_NOT_FOUND`
- `RESOURCE_NOT_AVAILABLE`
- `MILESTONE_NOT_FOUND`
- `BUDGET_NOT_FOUND`
- `EXPENSE_NOT_FOUND`
- `RISK_NOT_FOUND`
- `REPORT_GENERATION_FAILED`
- `INVALID_REPORT_DATE_RANGE`
- `DATABASE_ERROR`

## UI Behavior

- Clicking `Edit` opens a modal first.
- Only changed fields are sent in `PATCH` requests.
- `Delete` asks for confirmation and then removes the selected record immediately.
- The `Projects` screen has its own project dropdown filter.
- The `Tasks`, `Resources`, `Milestones`, and `Expenses` screens each have their own project filter for linked data.
- Task and project status updates move cleanly through `PLANNED`, `IN_PROGRESS`, and `COMPLETED`.
- Project progress is derived from completed tasks and completed milestones instead of being guessed manually.
- Project status becomes `COMPLETED` only when the project reaches `100%` and all tracked work items are complete.
- Task tables and reports show consistent `yyyy-MM-dd` dates.
- Reports are generated per project and include summary metrics plus detailed task rows.

## How To Run

1. Create the database.

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pm_db;"
```

2. Import the schema and seeded data.

```powershell
Get-Content .\sql\schema.sql | mysql -u root -p pm_db
```

3. Confirm `.env` contains the local database settings.

```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=pm_db
DB_USER=root
DB_PASSWORD=your_DB_password
PM_PORT=8080
```

4. Compile the project.

```powershell
javac -cp ".;lib/mysql-connector-j-9.3.0.jar" -d out (Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object FullName)
```

5. Run the server.

```powershell
java -cp ".;out;lib/mysql-connector-j-9.3.0.jar" com.vertex.pm.Main
```

6. Open the UI.

```text
http://localhost:8080
```

7. If you want ready-made values for the create forms, open:

```text
ui-create-samples.txt
```

## Main API Endpoints

- `GET /api/projects`
- `GET /api/projects/{id}`
- `POST /api/projects`
- `PATCH /api/projects/{id}`
- `PATCH /api/projects/{id}/progress`
- `DELETE /api/projects/{id}`
- `GET /api/projects/{id}/report`
- `GET /api/dashboard`
- `GET /api/monitoring`
- `GET /api/tasks`
- `POST /api/tasks`
- `PATCH /api/tasks/{id}`
- `DELETE /api/tasks/{id}`
- `GET /api/resources`
- `POST /api/resources`
- `PATCH /api/resources/{id}`
- `DELETE /api/resources/{id}`
- `GET /api/milestones`
- `POST /api/milestones`
- `PATCH /api/milestones/{id}`
- `DELETE /api/milestones/{id}`
- `GET /api/expenses`
- `POST /api/expenses`
- `PATCH /api/expenses/{id}`
- `DELETE /api/expenses/{id}`

## Quick Exception Testing

Start the server first, then run:

```powershell
.\test-exceptions.ps1
```

That script checks common exception paths such as:

- missing project
- missing task
- invalid task dates
- unavailable resource
- already completed task
- missing resource
- missing milestone
- missing expense
- invalid project dates
- missing project report
