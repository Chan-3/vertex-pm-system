CREATE DATABASE IF NOT EXISTS pm_db;
USE pm_db;

DROP TABLE IF EXISTS dependency;
DROP TABLE IF EXISTS risk;
DROP TABLE IF EXISTS expense;
DROP TABLE IF EXISTS milestone;
DROP TABLE IF EXISTS project_team_member;
DROP TABLE IF EXISTS resource;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS budget;
DROP TABLE IF EXISTS project;

CREATE TABLE project (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    description TEXT NOT NULL,
    manager_name VARCHAR(120) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    objectives TEXT NOT NULL,
    progress_pct INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE budget (
    budget_id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL UNIQUE,
    planned_amount DECIMAL(15,2) NOT NULL,
    actual_cost DECIMAL(15,2) NOT NULL DEFAULT 0,
    variance DECIMAL(15,2) NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_budget_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE task (
    id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    task_name VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    start_date DATE NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    assigned_to VARCHAR(120) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE resource (
    id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    name VARCHAR(120) NOT NULL,
    role VARCHAR(80) NOT NULL,
    availability BOOLEAN NOT NULL DEFAULT TRUE,
    skill_set VARCHAR(220) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_resource_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE project_team_member (
    member_id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    member_name VARCHAR(120) NOT NULL,
    member_role VARCHAR(80) NOT NULL,
    email VARCHAR(160) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_member_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE milestone (
    id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    name VARCHAR(160) NOT NULL,
    target_date DATE NOT NULL,
    completion_status BOOLEAN NOT NULL DEFAULT FALSE,
    description TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_milestone_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE expense (
    id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    expense_date DATE NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(80) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_expense_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE risk (
    id VARCHAR(32) PRIMARY KEY,
    project_id VARCHAR(32) NOT NULL,
    description TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    mitigation_plan TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_risk_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE
);

CREATE TABLE dependency (
    task_id VARCHAR(32) NOT NULL,
    depends_on_task_id VARCHAR(32) NOT NULL,
    project_id VARCHAR(32) NOT NULL,
    dependency_type VARCHAR(40) NOT NULL DEFAULT 'FINISH_TO_START',
    PRIMARY KEY (task_id, depends_on_task_id),
    CONSTRAINT fk_dependency_project FOREIGN KEY (project_id) REFERENCES project(id) ON DELETE CASCADE,
    CONSTRAINT fk_dependency_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
    CONSTRAINT fk_dependency_predecessor FOREIGN KEY (depends_on_task_id) REFERENCES task(id) ON DELETE CASCADE
);

INSERT INTO project (id, name, description, manager_name, start_date, end_date, status, objectives, progress_pct) VALUES
('PRJ-EV-LINE', 'Electric Vehicle Production Line Setup', 'Set up a dedicated electric vehicle production line for the Vertex E-Crossover program at the Chennai plant.', 'Priya Raman', '2026-01-06', '2026-09-30', 'IN_PROGRESS', 'Complete line design, vendor procurement, equipment installation, pilot build validation, and launch readiness for EV mass production.', 62),
('PRJ-ENG-OPT', 'Engine Assembly Optimization', 'Optimize the turbo-petrol engine assembly line to reduce takt loss and improve first-pass quality.', 'Arjun Mehta', '2026-02-03', '2026-08-28', 'IN_PROGRESS', 'Rebalance workstations, improve tooling uptime, strengthen supplier quality checks, and sign off a stable productivity plan.', 54),
('PRJ-ADAS-INT', 'Autonomous Driving Module Integration', 'Integrate ADAS control modules, validation environments, and production release governance for highway assist.', 'Nisha Varma', '2026-01-20', '2026-11-27', 'IN_PROGRESS', 'Freeze architecture, integrate compute stack, validate software behavior, complete cybersecurity checks, and release to production.', 47);

INSERT INTO budget (budget_id, project_id, planned_amount, actual_cost, variance) VALUES
('BGT-PRJ-EV-LINE', 'PRJ-EV-LINE', 18500000.00, 6155000.00, 12345000.00),
('BGT-PRJ-ENG-OPT', 'PRJ-ENG-OPT', 9400000.00, 2245000.00, 7155000.00),
('BGT-PRJ-ADAS-INT', 'PRJ-ADAS-INT', 22300000.00, 7625000.00, 14675000.00);

INSERT INTO resource (id, project_id, name, role, availability, skill_set) VALUES
('RES-EV-01', 'PRJ-EV-LINE', 'Rohit Anand', 'Manufacturing Engineer', TRUE, 'Line balancing, EV assembly, process capability'),
('RES-EV-02', 'PRJ-EV-LINE', 'Meghana Iyer', 'Program Manager', TRUE, 'Stage-gate planning, vendor coordination, launch reviews'),
('RES-EV-03', 'PRJ-EV-LINE', 'Sandeep Kulkarni', 'Quality Lead', TRUE, 'PFMEA, control plan, end-of-line validation'),
('RES-EV-04', 'PRJ-EV-LINE', 'Divya Shah', 'Automation Technician', TRUE, 'Robot commissioning, PLC tuning, teach points'),
('RES-EN-01', 'PRJ-ENG-OPT', 'Karthik Rao', 'Industrial Engineer', TRUE, 'Cycle time studies, ergonomics, kaizen'),
('RES-EN-02', 'PRJ-ENG-OPT', 'Farah Khan', 'Production Manager', TRUE, 'Capacity planning, operations coordination'),
('RES-EN-03', 'PRJ-ENG-OPT', 'Abhishek Nair', 'Maintenance Planner', FALSE, 'Downtime planning, spares control, maintenance windows'),
('RES-EN-04', 'PRJ-ENG-OPT', 'Lavanya Murthy', 'Supplier Quality Engineer', TRUE, 'Gauge validation, APQP, incoming inspection'),
('RES-AD-01', 'PRJ-ADAS-INT', 'Vikram Sethi', 'Systems Engineer', TRUE, 'ECU integration, CAN architecture, diagnostics'),
('RES-AD-02', 'PRJ-ADAS-INT', 'Ananya Das', 'Software Release Manager', TRUE, 'Release planning, regression governance, OTA readiness'),
('RES-AD-03', 'PRJ-ADAS-INT', 'Ritesh Sen', 'Validation Lead', TRUE, 'Scenario testing, SIL/HIL, defect triage'),
('RES-AD-04', 'PRJ-ADAS-INT', 'Sneha Joseph', 'Cybersecurity Analyst', TRUE, 'Threat modeling, secure boot, vulnerability review');

INSERT INTO project_team_member (member_id, project_id, member_name, member_role, email) VALUES
('MEM-EV-01', 'PRJ-EV-LINE', 'Priya Raman', 'Project Manager', 'priya.raman@vertexauto.com'),
('MEM-EV-02', 'PRJ-EV-LINE', 'Rohit Anand', 'Manufacturing Engineer', 'rohit.anand@vertexauto.com'),
('MEM-EV-03', 'PRJ-EV-LINE', 'Sandeep Kulkarni', 'Quality Lead', 'sandeep.kulkarni@vertexauto.com'),
('MEM-EV-04', 'PRJ-EV-LINE', 'Divya Shah', 'Automation Technician', 'divya.shah@vertexauto.com'),
('MEM-EN-01', 'PRJ-ENG-OPT', 'Arjun Mehta', 'Project Manager', 'arjun.mehta@vertexauto.com'),
('MEM-EN-02', 'PRJ-ENG-OPT', 'Karthik Rao', 'Industrial Engineer', 'karthik.rao@vertexauto.com'),
('MEM-EN-03', 'PRJ-ENG-OPT', 'Farah Khan', 'Production Manager', 'farah.khan@vertexauto.com'),
('MEM-EN-04', 'PRJ-ENG-OPT', 'Lavanya Murthy', 'Supplier Quality Engineer', 'lavanya.murthy@vertexauto.com'),
('MEM-AD-01', 'PRJ-ADAS-INT', 'Nisha Varma', 'Project Manager', 'nisha.varma@vertexauto.com'),
('MEM-AD-02', 'PRJ-ADAS-INT', 'Vikram Sethi', 'Systems Engineer', 'vikram.sethi@vertexauto.com'),
('MEM-AD-03', 'PRJ-ADAS-INT', 'Ananya Das', 'Software Release Manager', 'ananya.das@vertexauto.com'),
('MEM-AD-04', 'PRJ-ADAS-INT', 'Sneha Joseph', 'Cybersecurity Analyst', 'sneha.joseph@vertexauto.com');

INSERT INTO milestone (id, project_id, name, target_date, completion_status, description) VALUES
('MLS-EV-01', 'PRJ-EV-LINE', 'Layout And Design Freeze', '2026-02-28', TRUE, 'Approve final line layout, battery pack flow, and safety zoning.'),
('MLS-EV-02', 'PRJ-EV-LINE', 'Equipment Installation Complete', '2026-05-15', TRUE, 'Complete conveyor, lift assist, and robot installation.'),
('MLS-EV-03', 'PRJ-EV-LINE', 'Pilot Build Validation', '2026-07-30', FALSE, 'Run pilot builds and validate quality gates.'),
('MLS-EV-04', 'PRJ-EV-LINE', 'Launch Readiness Review', '2026-09-20', FALSE, 'Approve start-of-production readiness.'),
('MLS-EN-01', 'PRJ-ENG-OPT', 'Current State Study Complete', '2026-03-05', TRUE, 'Sign off current state cycle loss analysis.'),
('MLS-EN-02', 'PRJ-ENG-OPT', 'Balanced Workstation Release', '2026-05-19', TRUE, 'Release balanced workstation design for implementation.'),
('MLS-EN-03', 'PRJ-ENG-OPT', 'Line Trial Acceptance', '2026-07-10', FALSE, 'Approve weekend line trial and output stability.'),
('MLS-EN-04', 'PRJ-ENG-OPT', 'Productivity Sign-off', '2026-08-22', FALSE, 'Sign off final productivity gate.'),
('MLS-AD-01', 'PRJ-ADAS-INT', 'Architecture Baseline Approved', '2026-03-21', TRUE, 'Approve ADAS architecture baseline and interfaces.'),
('MLS-AD-02', 'PRJ-ADAS-INT', 'Vehicle Integration Sprint Complete', '2026-06-26', FALSE, 'Integrate compute unit and core software stack into prototype fleet.'),
('MLS-AD-03', 'PRJ-ADAS-INT', 'Road Validation Complete', '2026-09-25', FALSE, 'Complete road validation scenarios and close defects.'),
('MLS-AD-04', 'PRJ-ADAS-INT', 'Release To Production', '2026-11-20', FALSE, 'Approve production release package.');

INSERT INTO expense (id, project_id, expense_date, description, category, amount) VALUES
('EXP-EV-01', 'PRJ-EV-LINE', '2026-02-10', 'Battery module conveyor procurement', 'Equipment', 4200000.00),
('EXP-EV-02', 'PRJ-EV-LINE', '2026-03-16', 'Body shop robot reprogramming', 'Labor', 980000.00),
('EXP-EV-03', 'PRJ-EV-LINE', '2026-05-28', 'Pilot build material freight', 'Logistics', 365000.00),
('EXP-EV-04', 'PRJ-EV-LINE', '2026-06-18', 'Safety interlock upgrade', 'Infrastructure', 610000.00),
('EXP-EN-01', 'PRJ-ENG-OPT', '2026-03-01', 'Torque tool calibration package', 'Equipment', 1220000.00),
('EXP-EN-02', 'PRJ-ENG-OPT', '2026-03-24', 'Line ergonomics consulting', 'Labor', 410000.00),
('EXP-EN-03', 'PRJ-ENG-OPT', '2026-04-22', 'Supplier gauge certification', 'Quality', 285000.00),
('EXP-EN-04', 'PRJ-ENG-OPT', '2026-06-30', 'Weekend trial shift overtime', 'Labor', 330000.00),
('EXP-AD-01', 'PRJ-ADAS-INT', '2026-02-14', 'ADAS compute bench hardware', 'Equipment', 5300000.00),
('EXP-AD-02', 'PRJ-ADAS-INT', '2026-04-04', 'Scenario simulation licenses', 'Software', 1190000.00),
('EXP-AD-03', 'PRJ-ADAS-INT', '2026-05-26', 'Prototype vehicle logistics', 'Logistics', 460000.00),
('EXP-AD-04', 'PRJ-ADAS-INT', '2026-07-11', 'Cybersecurity assessment', 'Validation', 675000.00);

INSERT INTO risk (id, project_id, description, severity, status, mitigation_plan) VALUES
('RSK-EV-01', 'PRJ-EV-LINE', 'Delayed battery tray fixture delivery can compress pilot build preparation.', 'HIGH', 'OPEN', 'Track vendor delivery weekly and hold alternate fixture sourcing ready.'),
('RSK-EV-02', 'PRJ-EV-LINE', 'Robot safety acceptance may slip if interlock validation is deferred.', 'MEDIUM', 'OPEN', 'Advance validation plan and reserve weekend safety review slots.'),
('RSK-EN-01', 'PRJ-ENG-OPT', 'Maintenance planner availability is constrained during shutdown preparation.', 'HIGH', 'OPEN', 'Reassign downtime planning support to plant maintenance backup lead.'),
('RSK-EN-02', 'PRJ-ENG-OPT', 'Supplier torque traceability data remains inconsistent.', 'MEDIUM', 'OPEN', 'Run supplier audit and strengthen incoming verification.'),
('RSK-AD-01', 'PRJ-ADAS-INT', 'CAN integration defects may increase validation rig downtime.', 'HIGH', 'OPEN', 'Add daily defect triage and pre-rig smoke testing.'),
('RSK-AD-02', 'PRJ-ADAS-INT', 'Cybersecurity remediation could delay release readiness.', 'MEDIUM', 'OPEN', 'Start threat review earlier and maintain patch review cadence.');

INSERT INTO task (id, project_id, task_name, description, start_date, due_date, status, priority, assigned_to) VALUES
('TSK-EV-01', 'PRJ-EV-LINE', 'Finalize EV line layout and material flow study', 'Complete detailed design for body, battery, and final assembly flow.', '2026-01-06', '2026-01-24', 'COMPLETED', 'HIGH', 'Rohit Anand'),
('TSK-EV-02', 'PRJ-EV-LINE', 'Approve battery pack station concept and safety zoning', 'Lock station concept, operator zones, and safety interfaces.', '2026-01-20', '2026-02-07', 'COMPLETED', 'HIGH', 'Meghana Iyer'),
('TSK-EV-03', 'PRJ-EV-LINE', 'Release RFQ for conveyor and torque systems', 'Issue RFQ package for conveyor, lift assist, and torque tools.', '2026-02-01', '2026-02-19', 'COMPLETED', 'HIGH', 'Meghana Iyer'),
('TSK-EV-04', 'PRJ-EV-LINE', 'Confirm supplier technical and commercial alignment', 'Finish technical reviews and finalize supplier scope alignment.', '2026-02-18', '2026-03-10', 'COMPLETED', 'MEDIUM', 'Meghana Iyer'),
('TSK-EV-05', 'PRJ-EV-LINE', 'Install main line conveyor and battery marriage station', 'Install physical infrastructure for EV assembly.', '2026-03-15', '2026-04-22', 'COMPLETED', 'HIGH', 'Divya Shah'),
('TSK-EV-06', 'PRJ-EV-LINE', 'Commission robot teach points for underbody sealing', 'Tune robot path logic and seal application sequence.', '2026-04-10', '2026-05-06', 'IN_PROGRESS', 'HIGH', 'Divya Shah'),
('TSK-EV-07', 'PRJ-EV-LINE', 'Validate station ergonomics and operator walk paths', 'Verify operator movement and ergonomic standards.', '2026-05-05', '2026-05-27', 'IN_PROGRESS', 'MEDIUM', 'Rohit Anand'),
('TSK-EV-08', 'PRJ-EV-LINE', 'Run pilot build quality gate and containment plan', 'Execute pilot build and defect containment checks.', '2026-06-01', '2026-07-18', 'IN_PROGRESS', 'HIGH', 'Sandeep Kulkarni'),
('TSK-EV-09', 'PRJ-EV-LINE', 'Train launch team on diagnostics and escalation', 'Prepare launch team for line-side issue handling.', '2026-07-15', '2026-08-20', 'PLANNED', 'MEDIUM', 'Sandeep Kulkarni'),
('TSK-EV-10', 'PRJ-EV-LINE', 'Complete production launch readiness review', 'Approve SOP readiness and hand over to operations.', '2026-08-25', '2026-09-20', 'PLANNED', 'HIGH', 'Meghana Iyer'),
('TSK-EN-01', 'PRJ-ENG-OPT', 'Map current engine assembly cycle times and losses', 'Document current line losses and bottleneck stations.', '2026-02-03', '2026-02-20', 'COMPLETED', 'HIGH', 'Karthik Rao'),
('TSK-EN-02', 'PRJ-ENG-OPT', 'Approve revised operator balance chart', 'Freeze revised operator loading for head line.', '2026-02-18', '2026-03-04', 'COMPLETED', 'HIGH', 'Farah Khan'),
('TSK-EN-03', 'PRJ-ENG-OPT', 'Issue tooling changes for preload checks', 'Release tooling improvement actions to suppliers.', '2026-03-02', '2026-03-18', 'COMPLETED', 'HIGH', 'Lavanya Murthy'),
('TSK-EN-04', 'PRJ-ENG-OPT', 'Validate torque traceability with suppliers', 'Confirm torque traceability data integrity.', '2026-03-15', '2026-04-05', 'COMPLETED', 'MEDIUM', 'Lavanya Murthy'),
('TSK-EN-05', 'PRJ-ENG-OPT', 'Reconfigure workstation reach zones', 'Update pick locations and reach envelopes.', '2026-04-01', '2026-04-28', 'COMPLETED', 'MEDIUM', 'Karthik Rao'),
('TSK-EN-06', 'PRJ-ENG-OPT', 'Deploy digital torque prompts and job aids', 'Roll out updated guidance at each station.', '2026-04-22', '2026-05-16', 'IN_PROGRESS', 'MEDIUM', 'Farah Khan'),
('TSK-EN-07', 'PRJ-ENG-OPT', 'Execute downtime analysis and PM reset', 'Reschedule preventive maintenance to reduce losses.', '2026-05-12', '2026-06-07', 'IN_PROGRESS', 'HIGH', 'Abhishek Nair'),
('TSK-EN-08', 'PRJ-ENG-OPT', 'Run balanced line trial with weekend crew', 'Validate output and line balance with production crew.', '2026-06-10', '2026-07-08', 'IN_PROGRESS', 'HIGH', 'Farah Khan'),
('TSK-EN-09', 'PRJ-ENG-OPT', 'Release updated standard work and audits', 'Publish final standard work documents and layered checks.', '2026-07-10', '2026-08-01', 'PLANNED', 'MEDIUM', 'Karthik Rao'),
('TSK-EN-10', 'PRJ-ENG-OPT', 'Close productivity gate with KPI sign-off', 'Approve final productivity performance against target.', '2026-08-04', '2026-08-22', 'PLANNED', 'HIGH', 'Farah Khan'),
('TSK-AD-01', 'PRJ-ADAS-INT', 'Freeze ADAS electrical architecture and interfaces', 'Approve network interfaces, ECU links, and diagnostics flows.', '2026-01-20', '2026-02-14', 'COMPLETED', 'HIGH', 'Vikram Sethi'),
('TSK-AD-02', 'PRJ-ADAS-INT', 'Define perception stack release scope', 'Lock scope and feature gating for highway assist.', '2026-02-10', '2026-03-03', 'COMPLETED', 'HIGH', 'Ananya Das'),
('TSK-AD-03', 'PRJ-ADAS-INT', 'Procure compute benches and harnesses', 'Secure hardware needed for lab and vehicle integration.', '2026-03-01', '2026-03-29', 'COMPLETED', 'HIGH', 'Ananya Das'),
('TSK-AD-04', 'PRJ-ADAS-INT', 'Set up SIL and HIL validation environments', 'Create stable software and hardware-in-loop environments.', '2026-03-24', '2026-04-25', 'COMPLETED', 'HIGH', 'Ritesh Sen'),
('TSK-AD-05', 'PRJ-ADAS-INT', 'Integrate compute module with CAN gateway', 'Integrate core compute and diagnostic handshake.', '2026-04-18', '2026-05-30', 'IN_PROGRESS', 'HIGH', 'Vikram Sethi'),
('TSK-AD-06', 'PRJ-ADAS-INT', 'Deploy release candidate to prototype fleet', 'Flash and stabilize release candidate in prototype vehicles.', '2026-05-25', '2026-06-20', 'IN_PROGRESS', 'HIGH', 'Ananya Das'),
('TSK-AD-07', 'PRJ-ADAS-INT', 'Execute highway assist scenario validation', 'Run closed-track and road scenarios, triage defects.', '2026-06-18', '2026-08-08', 'IN_PROGRESS', 'HIGH', 'Ritesh Sen'),
('TSK-AD-08', 'PRJ-ADAS-INT', 'Complete cybersecurity hardening review', 'Finish secure boot and vulnerability review closure.', '2026-07-12', '2026-08-26', 'PLANNED', 'HIGH', 'Sneha Joseph'),
('TSK-AD-09', 'PRJ-ADAS-INT', 'Prepare release documentation and evidence', 'Prepare evidence for release governance and homologation.', '2026-08-20', '2026-10-02', 'PLANNED', 'MEDIUM', 'Ananya Das'),
('TSK-AD-10', 'PRJ-ADAS-INT', 'Approve production release readiness', 'Approve production release and support readiness.', '2026-10-06', '2026-11-20', 'PLANNED', 'HIGH', 'Nisha Varma');

INSERT INTO dependency (task_id, depends_on_task_id, project_id, dependency_type) VALUES
('TSK-EV-02', 'TSK-EV-01', 'PRJ-EV-LINE', 'FINISH_TO_START'),
('TSK-EV-03', 'TSK-EV-02', 'PRJ-EV-LINE', 'FINISH_TO_START'),
('TSK-EV-05', 'TSK-EV-03', 'PRJ-EV-LINE', 'FINISH_TO_START'),
('TSK-EV-08', 'TSK-EV-06', 'PRJ-EV-LINE', 'FINISH_TO_START'),
('TSK-EN-02', 'TSK-EN-01', 'PRJ-ENG-OPT', 'FINISH_TO_START'),
('TSK-EN-05', 'TSK-EN-03', 'PRJ-ENG-OPT', 'FINISH_TO_START'),
('TSK-EN-08', 'TSK-EN-07', 'PRJ-ENG-OPT', 'FINISH_TO_START'),
('TSK-AD-02', 'TSK-AD-01', 'PRJ-ADAS-INT', 'FINISH_TO_START'),
('TSK-AD-05', 'TSK-AD-04', 'PRJ-ADAS-INT', 'FINISH_TO_START'),
('TSK-AD-10', 'TSK-AD-09', 'PRJ-ADAS-INT', 'FINISH_TO_START');
