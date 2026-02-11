-- V1__init.sql: Initial schema for Warehouse Employee Management System

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    badge_id VARCHAR(32) NOT NULL UNIQUE,
    name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    department VARCHAR(64),
    shift_group VARCHAR(64),
    hire_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE attendance_event (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    event_type VARCHAR(16) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    geofence_valid BOOLEAN DEFAULT TRUE,
    device_info VARCHAR(128),
    hours_worked DECIMAL(5,2),
    correction_requested BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_template (
    id SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    recurrence_pattern VARCHAR(32),
    blackout_dates TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE shift_assignment (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    shift_template_id INTEGER NOT NULL REFERENCES shift_template(id),
    assignment_date DATE NOT NULL,
    conflict_detected BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE leave_request (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    leave_type VARCHAR(32) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    accrual_balance DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE certification (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    cert_type VARCHAR(64) NOT NULL,
    issue_date DATE NOT NULL,
    expiry_date DATE NOT NULL,
    proof_document VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE safety_incident (
    id SERIAL PRIMARY KEY,
    severity VARCHAR(32) NOT NULL,
    location VARCHAR(128),
    description TEXT,
    status VARCHAR(32) NOT NULL,
    involved_employee_ids TEXT,
    investigation_notes TEXT,
    osha_exported BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE asset (
    id SERIAL PRIMARY KEY,
    asset_tag VARCHAR(64) NOT NULL UNIQUE,
    asset_type VARCHAR(64) NOT NULL,
    assigned_employee_id INTEGER REFERENCES employee(id),
    checkout_time TIMESTAMP,
    return_time TIMESTAMP,
    certification_required VARCHAR(64),
    condition_state VARCHAR(32),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE performance_review (
    id SERIAL PRIMARY KEY,
    employee_id INTEGER NOT NULL REFERENCES employee(id),
    review_cycle VARCHAR(32) NOT NULL,
    goals TEXT,
    competencies TEXT,
    rating INTEGER,
    comments TEXT,
    supervisor_ack BOOLEAN DEFAULT FALSE,
    employee_ack BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_log (
    id SERIAL PRIMARY KEY,
    actor VARCHAR(64) NOT NULL,
    entity VARCHAR(64) NOT NULL,
    entity_id INTEGER NOT NULL,
    action VARCHAR(32) NOT NULL,
    before_state TEXT,
    after_state TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    immutable BOOLEAN DEFAULT TRUE
);
