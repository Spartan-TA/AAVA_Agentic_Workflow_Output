-- Flyway migration for Warehouse EMS initial schema
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    badge_id VARCHAR(50) UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    department VARCHAR(100),
    position VARCHAR(100),
    hire_date DATE,
    status VARCHAR(50)
);

CREATE TABLE attendance_events (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    device_id VARCHAR(100),
    location VARCHAR(255),
    latitude VARCHAR(50),
    longitude VARCHAR(50),
    status VARCHAR(50),
    schedule_id BIGINT,
    correction BOOLEAN,
    correction_reason VARCHAR(255),
    approved_by BIGINT,
    approved_at TIMESTAMP,
    created_at TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (approved_by) REFERENCES employees(id)
);

CREATE TABLE shift_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence VARCHAR(50),
    overtime_threshold_hours INT,
    overtime_multiplier DOUBLE,
    description VARCHAR(255),
    active BOOLEAN,
    -- daysOfWeek handled by shift_template_days
    UNIQUE(name)
);

CREATE TABLE shift_template_days (
    shift_template_id BIGINT,
    day_of_week INT,
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id)
);

CREATE TABLE schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    shift_template_id BIGINT NOT NULL,
    schedule_date DATE NOT NULL,
    status VARCHAR(50),
    notes VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (shift_template_id) REFERENCES shift_templates(id)
);

CREATE TABLE blackout_dates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date DATE NOT NULL,
    reason VARCHAR(255),
    type VARCHAR(100),
    department VARCHAR(100)
);

CREATE TABLE leave_requests (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    type VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days DECIMAL(5,2),
    status VARCHAR(50),
    reason VARCHAR(255),
    approver_id BIGINT,
    approved_at TIMESTAMP,
    approval_notes VARCHAR(255),
    balance_impact DECIMAL(5,2),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approver_id) REFERENCES employees(id)
);

CREATE TABLE leave_balances (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(50),
    balance DECIMAL(5,2),
    accrual_rate DECIMAL(5,2),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE safety_incidents (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    incident_number VARCHAR(100) UNIQUE NOT NULL,
    incident_date DATE NOT NULL,
    incident_time TIMESTAMP,
    type VARCHAR(50),
    severity VARCHAR(50),
    location VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    status VARCHAR(50),
    immediate_action_taken VARCHAR(255),
    root_cause VARCHAR(255),
    corrective_actions VARCHAR(255),
    investigation_started_at TIMESTAMP,
    resolved_at TIMESTAMP,
    osha_recordable BOOLEAN,
    days_away_from_work INT,
    created_at TIMESTAMP,
    reported_by BIGINT,
    investigator_id BIGINT,
    FOREIGN KEY (reported_by) REFERENCES employees(id),
    FOREIGN KEY (investigator_id) REFERENCES employees(id)
);

CREATE TABLE incident_involved_employees (
    safety_incident_id BIGINT,
    employee_id BIGINT,
    FOREIGN KEY (safety_incident_id) REFERENCES safety_incidents(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) UNIQUE NOT NULL,
    description VARCHAR(255),
    active BOOLEAN
);

CREATE TABLE employee_certifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    certification_id BIGINT NOT NULL,
    status VARCHAR(50),
    issue_date DATE,
    expiry_date DATE,
    notes VARCHAR(255),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (certification_id) REFERENCES certifications(id)
);
