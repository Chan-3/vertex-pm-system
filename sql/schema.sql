CREATE DATABASE IF NOT EXISTS pm_db;
USE pm_db;

CREATE TABLE project (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) CHECK (status IN ('PLANNED','IN_PROGRESS','COMPLETED'))
);

CREATE TABLE task (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT,
    name VARCHAR(100),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE resource (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT,
    name VARCHAR(100),
    role VARCHAR(50),
    available BOOLEAN,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE assignment (
    task_id INT,
    resource_id INT,
    PRIMARY KEY(task_id, resource_id),
    FOREIGN KEY (task_id) REFERENCES task(id),
    FOREIGN KEY (resource_id) REFERENCES resource(id)
);

CREATE TABLE milestone (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT,
    name VARCHAR(100),
    target_date DATE,
    completed BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE dependency (
    task_id INT,
    depends_on_task_id INT,
    PRIMARY KEY(task_id, depends_on_task_id)
);

CREATE TABLE risk (
    id INT PRIMARY KEY,
    project_id INT,
    description VARCHAR(255),
    severity VARCHAR(20),
    FOREIGN KEY (project_id) REFERENCES project(id)
);

CREATE TABLE budget (
    project_id INT PRIMARY KEY,
    total_amount DECIMAL(10,2)
);

CREATE TABLE expense (
    id INT PRIMARY KEY AUTO_INCREMENT,
    project_id INT,
    amount DECIMAL(10,2),
    category VARCHAR(50),
    date DATE,
    FOREIGN KEY (project_id) REFERENCES project(id)
);

INSERT INTO project (id, name, description, start_date, end_date, status) VALUES
(1, 'Vertex PM Dashboard', 'Web-based project management subsystem with repository failover and risk tracking.', '2026-03-19', '2026-05-28', 'IN_PROGRESS'),
(2, 'ERP Resource Upgrade', 'Modernization of resource allocation flow with milestone and budget visibility.', '2026-03-31', '2026-06-12', 'PLANNED');

INSERT INTO task (id, project_id, name, start_date, end_date, status) VALUES
(101, 1, 'Build controller endpoints', '2026-04-04', '2026-04-20', 'IN_PROGRESS'),
(102, 1, 'Create UI dashboard', '2026-04-08', '2026-04-24', 'IN_PROGRESS'),
(103, 1, 'Add testing checklist', '2026-04-14', '2026-04-26', 'NOT_STARTED'),
(104, 2, 'Finalize schema', '2026-04-15', '2026-04-23', 'IN_PROGRESS'),
(105, 2, 'Implement cloud fallback', '2026-04-19', '2026-05-03', 'NOT_STARTED');

INSERT INTO resource (id, project_id, name, role, available) VALUES
(501, 1, 'Asha', 'Backend Developer', TRUE),
(502, 1, 'Kiran', 'Frontend Developer', TRUE),
(503, 2, 'Meera', 'Business Analyst', TRUE);

INSERT INTO assignment (task_id, resource_id) VALUES
(101, 501),
(102, 502),
(104, 503);

INSERT INTO milestone (id, project_id, name, target_date, completed) VALUES
(201, 1, 'Layered architecture review', '2026-04-21', FALSE),
(202, 1, 'UI delivery', '2026-04-30', FALSE),
(203, 2, 'Schema sign-off', '2026-04-25', FALSE);

INSERT INTO dependency (task_id, depends_on_task_id) VALUES
(103, 102),
(105, 104);

INSERT INTO risk (id, project_id, description, severity) VALUES
(301, 1, 'Database unavailable during demo', 'HIGH'),
(302, 1, 'Late UI refinements', 'MEDIUM'),
(303, 2, 'Requirement changes from stakeholders', 'MEDIUM');

INSERT INTO budget (project_id, total_amount) VALUES
(1, 180000.00),
(2, 95000.00);

INSERT INTO expense (id, project_id, amount, category, date) VALUES
(1, 1, 32000.00, 'Design', '2026-03-29'),
(2, 1, 58000.00, 'Development', '2026-04-09'),
(3, 2, 12000.00, 'Analysis', '2026-04-06'),
(4, 2, 15000.00, 'Infrastructure', '2026-04-13');
