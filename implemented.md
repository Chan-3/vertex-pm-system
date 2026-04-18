# Implemented Work

This file records what has been completed in the project.

## Completed Architecture

- Created the project in the blueprint package structure under `com.vertex.pm`
- Implemented layered flow:
  - UI
  - Controller
  - Service
  - Repository
- Added separate repository strategies for:
  - MySQL repository
  - API repository
- Added a factory to choose the correct repository
- Added failover behavior from MySQL to API

## Completed Models

- `Project`
- `Task`
- `Milestone`
- `Resource`
- `Budget`
- `Expense`
- `Dependency`
- `Risk`
- `ProjectStatus`
- `TaskStatus`
- `RiskSeverity`

## Completed Exception Handling

- `ProjectManagementException`
- `ProjectNotFoundException`
- `InvalidTaskDateException`
- `ResourceNotAvailableException`
- `DuplicateProjectException`
- `RepositoryException`

## Completed Service and Controller Logic

- Added `ProjectService` for validation and business rules
- Added `ProjectController` for HTTP request handling
- Added `SystemController` for dashboard, task, resource, budget, monitoring, and report endpoints
- Added project fetch, create, update, and delete endpoints
- Added task create, update-status, and delete endpoints
- Added task edit endpoint support
- Added resource create, edit, and delete endpoints
- Added expense create, edit, and delete endpoints
- Added milestone create, edit, complete, and delete endpoints
- Added monitoring, dashboard, and reports endpoints

## Completed Repository and Utility Logic

- Added `ProjectRepository` interface
- Added `MySqlProjectRepository`
- Added `ApiProjectRepository`
- Added `FailoverProjectRepository`
- Added `RepositoryFactory`
- Added `DatabaseConnectionManager` as a Singleton
- Added `CloudApiClient` with fallback sample data
- Added `EnvConfig` for optional `.env` loading and `DATA_SOURCE` switching
- Added `JsonUtil` for local JSON handling
- Added `StaticFileHandler` for serving frontend files
- Added `AppLogger` for warnings and info logs

## Completed UI

- Created web UI with:
  - top navigation bar
  - dashboard overview
  - project management screen with table and actions
  - task management screen with table and actions
  - resource management screen with table and actions
  - milestone management screen with completion action
  - budget and expense screen
  - monitoring screen
  - reports screen
- Connected the UI to backend endpoints using `fetch`
- Added automatic refresh after changes
- Added clear success and error messages
- Added edit flows for remaining module actions

## Completed Database Support

- Added `sql/schema.sql` with the required project, task, resource, assignment, milestone, dependency, budget, and expense tables

## Principles Applied In Code

- SRP: separate controller, service, repository, util, and model classes
- OCP: repository abstraction allows extension
- DIP: service depends on `ProjectRepository`
- GRASP Controller: `ProjectController`
- High Cohesion: focused classes
- Low Coupling: interfaces and repository factory
- Information Expert: `Project`, `Task`, and `Budget`
- Strategy: MySQL vs API repositories
- Factory: `RepositoryFactory`
- Singleton: `DatabaseConnectionManager`
- Facade-like failover wrapper: `FailoverProjectRepository`

## Notes

- The MySQL repository is included structurally and attempts connection checks
- The project remains runnable even without MySQL because the API fallback is active
- `.env.example` is included for configuration guidance
- `schema.sql` now includes sample insert data in addition to table creation
- Comments were added throughout the code to align with the implementation guide
