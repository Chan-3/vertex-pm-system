# Vertex PM System

This project is a Project Management subsystem built by following `PM_System_Blueprint.md` and `PM_Implementation_Guide.md`.

It uses:

- Java backend
- Web UI with HTML, CSS, and JavaScript
- Layered architecture: `UI -> Controller -> Service -> Repository`
- Repository switching with MySQL as the preferred source and API fallback
- Custom exceptions, validation, and logging
- `.env`-style configuration for data source switching

## Structure

```text
vertex-pm-system
|-- src/com/vertex/pm
|   |-- controller
|   |-- exception
|   |-- factory
|   |-- model
|   |-- repository
|   |   |-- api
|   |   `-- mysql
|   |-- service
|   `-- util
|-- sql
`-- ui
```

## What it does

- Shows a professional PM web UI with navigation and module screens
- Supports dashboard, projects, tasks, resources, milestones, budget, monitoring, and reports screens
- Exposes REST-like endpoints for those modules
- Validates project and task rules
- Tries local MySQL first
- Falls back to cloud-style API data if MySQL is unavailable
- Follows `PM_UI_Blueprint.md` with tables, actions, forms, refresh behavior, and clear UI messages

## Execution order

Run the project in this order:

1. Start MySQL
2. Create the database objects and sample data from `sql/schema.sql`
3. Set `.env` if you want local DB mode
4. Compile and run the Java backend
5. Open the UI in the browser

## Step 1: Create the database

Run:

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS pm_db;"
```

## Step 2: Import the schema and sample data

Run:

```powershell
Get-Content .\sql\schema.sql | mysql -u root -p pm_db
```

This file now includes:

- table creation
- foreign keys
- sample insert data

## Step 3: Configure `.env`

Example:

```env
DATA_SOURCE=auto
DB_HOST=localhost
DB_PORT=3306
DB_NAME=pm_db
DB_USER=root
DB_PASSWORD=your_password
```

Use:

- `local` for MySQL only
- `cloud` for API fallback data only
- `auto` to try MySQL first and fall back to API

You can also set the server port:

```env
PM_PORT=8080
```

## Step 4: Compile and run backend

Open PowerShell in `vertex-pm-system` and run:

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp "out;lib/*" com.vertex.pm.Main
```

Wait until the console prints:

```text
Vertex PM server started at http://localhost:8080
```

## Step 5: Open UI

```text
http://localhost:8080
```

The backend must be running before the UI will load data.

## If port 8080 is already in use

Find the process using the port:

```powershell
Get-NetTCPConnection -LocalPort 8080 | Select-Object LocalAddress,LocalPort,State,OwningProcess
```

Stop that process if you want to reuse `8080`:

```powershell
Stop-Process -Id <PID> -Force
```

Or set another port in `.env`:

```env
PM_PORT=8081
```

Then run the backend and open:

```text
http://localhost:8081
```

## Optional MySQL configuration

The app checks environment variables and optional `.env` values:

- `PM_DB_URL`
- `PM_DB_USER`
- `PM_DB_PASSWORD`
- `DATA_SOURCE`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`

If they are not set, it defaults to:

```text
jdbc:mysql://localhost:3306/vertex_pm
root
root
```

If MySQL is unavailable, the application automatically switches to the fallback API repository.
If `DATA_SOURCE=local` but MySQL is not reachable, the app now falls back to cloud/API data so one available source can still serve the UI.

The MySQL JDBC driver is included in:

- [mysql-connector-j-9.6.0.jar](/c:/Users/Chandu/OneDrive/Desktop/projectManagement/vertex-pm-system/lib/mysql-connector-j-9.6.0.jar)

## Data source selection

Use `DATA_SOURCE` in `.env` or your environment:

- `local` -> use MySQL only
- `cloud` -> use API only
- `auto` -> try local first, then fallback to API

## SQL schema

The SQL schema from the blueprint is included in:

- [schema.sql](/c:/Users/Chandu/OneDrive/Desktop/projectManagement/vertex-pm-system/sql/schema.sql)
