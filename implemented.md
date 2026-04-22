# Implemented Work

This file records the current completed work in the direct `pm_db` version of Vertex PM System.

## 1. Architecture

- kept the project in a clean layered structure under `src/com/vertex/pm`
- used `controller -> service -> repository -> database`
- kept one active runtime data path: direct MySQL access to `pm_db`
- removed integration and failover behavior from the main runtime path

## 2. Domain Model

Implemented the main project management domain classes:

- `Project`
- `Task`
- `Resource`
- `TeamMember`
- `Milestone`
- `Expense`
- `Risk`
- `Budget`
- `Dependency`
- `ProjectMonitor`
- `ProjectReport`
- `ProjectStatus`
- `TaskStatus`
- `RiskSeverity`

## 3. Database Design

Updated `sql/schema.sql` so the live schema now includes:

- `project`
- `budget`
- `task`
- `resource`
- `project_team_member`
- `milestone`
- `expense`
- `risk`
- `dependency`

The schema now supports:

- a separate budget table instead of hiding budget values inside the project row only
- team member data for each project
- dependency data between tasks
- realistic car manufacturing seed data

## 4. Seed Data

Seeded 3 professional manufacturing programs:

- `Electric Vehicle Production Line Setup`
- `Engine Assembly Optimization`
- `Autonomous Driving Module Integration`

For each project, added:

- 10 realistic tasks
- linked resources
- named team members with emails
- milestone checkpoints
- expense entries
- open risk records
- task dependency rows
- budget data

## 5. Backend Endpoints

Implemented and kept active:

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

## 6. Update Behavior

- changed edit operations to modal-based partial updates
- kept `PATCH` as the update mechanism
- only changed fields are sent from the UI
- removed full child delete-and-recreate update logic
- kept delete operations as direct record deletes
- linked expense changes back to budget actual cost recalculation

## 7. Exception Handling

Expanded the exception catalog in `src/com/vertex/pm/exception/ExceptionType.java` and aligned the active code paths more closely with the project-management exception rules.

Implemented or actively used exception types include:

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

Improvements made in code:

- project date validation now throws `INVALID_PROJECT_DATES`
- missing resource lookups now throw `RESOURCE_NOT_FOUND`
- missing milestone lookups now throw `MILESTONE_NOT_FOUND`
- missing expense lookups now throw `EXPENSE_NOT_FOUND`
- patch and delete operations now fail clearly when the record does not exist

## 8. UI

Updated `ui/index.html`, `ui/app.js`, and `ui/style.css` so the UI now:

- opens edit modals before updating
- sends partial `PATCH` payloads only
- shows clearer start and due dates for tasks
- uses plain string enum values instead of object-shaped values
- supports direct delete with confirmation
- shows project report metrics plus milestone and task detail tables
- supports a dedicated project dropdown filter in the `Projects` screen
- supports separate project-based filters in the `Tasks`, `Resources`, `Milestones`, and `Expenses` screens
- shows project progress more clearly with progress bars and budget context
- keeps project progress and status aligned with completed tasks and milestones

## 9. Utilities

Kept and cleaned:

- `EnvConfig`
- `DatabaseConnectionManager`
- `JsonUtil`
- `IdGenerator`
- `AppLogger`
- `StaticFileHandler`

Important cleanup:

- `.env` now uses only `DB_*` keys plus `PM_PORT`
- `DatabaseConnectionManager` now reads only the direct local DB configuration path used by this project
- added `ui-create-samples.txt` so the UI forms have ready-to-copy example values for academic demo use

## 10. Verification

Verified during this refactor:

- source compiles with the local MySQL JDBC driver
- `sql/schema.sql` is ready to recreate `pm_db`
- status serialization returns readable strings
- project, task, milestone, resource, and expense patch flow remains active
- report generation includes meaningful project-specific output
- project progress is recalculated from task and milestone completion
- project search/filter behavior is wired into the UI flow
